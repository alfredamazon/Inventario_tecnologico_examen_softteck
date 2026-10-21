
import { Component, OnInit, Inject } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';

interface Activo {
  id: string;
  folio: string;
  serie: string;
  marca: string;
  categoria: string;
  estado: string;
  costo: number;
}

interface Categoria {
  id: number;
  nombre: string;
  codigoPrefijo: string;
}

interface ActivoRegistro {
  numeroSerie: string;
  marcaModelo: string;
  categoriaId: number | null;
  costoAdquisicion: number | null;
}

interface ActivoCreado {
  id: string;
  folioInventario: string;
  numeroSerie: string;
  marcaModelo: string;
  categoriaNombre: string;
  estado: string;
  costoAdquisicion: number;
}

interface ReporteZipResponse {
  status: number;
  message: string;
  fileName: string;
  fileBase64: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {

  private readonly apiUrl = 'http://localhost:8080/api/activos';
  private readonly categoriasUrl = 'http://localhost:8080/api/categorias';

  private tokenApi = '';

  activos: Activo[] = [];
  activosFiltrados: Activo[] = [];
  categorias: Categoria[] = [];

  filtroSerie = '';
  filtroMarca = '';
  filtroEstado = '';

  exportando = false;
  mostrarFormulario = false;
  guardando = false;
  sesionActiva = false;
  esEdicion = false;
  esAdmin = false;

  activoEditandoId: string | null = null;

  nuevo: ActivoRegistro = this.limpiarFormulario();

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: object
  ) {}

  ngOnInit(): void {
    this.limpiarSesion();
  }

  private obtenerHeaders(): HttpHeaders {
    return new HttpHeaders({
      Authorization: `Bearer ${this.tokenApi}`
    });
  }

  cambiarPerfil(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    const token = prompt(
      'Pega el JWT del perfil ADMIN o USER, sin escribir "Bearer ":'
    );

    if (!token?.trim()) {
      return;
    }

    this.limpiarSesion();
    this.procesarToken(token.trim());
  }

  private procesarToken(
    token: string,
    continuar?: () => void,
    silencioso = false
  ): void {
    try {
      const partes = token.split('.');

      if (partes.length !== 3) {
        throw new Error('El formato del JWT no es válido.');
      }

      const payloadBase64 = partes[1]
        .replace(/-/g, '+')
        .replace(/_/g, '/');

      const payload = JSON.parse(
        atob(
          payloadBase64.padEnd(
            payloadBase64.length +
              (4 - payloadBase64.length % 4) % 4,
            '='
          )
        )
      );

      const rol = String(payload.rol ?? '').toUpperCase();

      if (rol !== 'ADMIN' && rol !== 'USER') {
        throw new Error(
          'El token debe contener el rol ADMIN o USER en la propiedad "rol".'
        );
      }

      this.tokenApi = token;

      this.http.get<any[]>(this.apiUrl, {
        headers: this.obtenerHeaders()
      }).subscribe({
        next: (respuesta) => {
          this.esAdmin = rol === 'ADMIN';

          this.activos = (respuesta ?? []).map(
            activo => this.convertirActivo(activo)
          );

          this.buscar();
          this.sesionActiva = true;

          if (!silencioso) {
            alert(`Perfil ${rol} autenticado correctamente.`);
          }

          continuar?.();
        },
        error: (error) => {
          console.error('Error al consultar activos:', error);
          this.limpiarSesion();

          if (!silencioso) {
            alert(
              `No fue posible consultar el inventario. HTTP ${
                error.status || 'sin respuesta'
              }. Verifica el token y el backend.`
            );
          }
        }
      });
    } catch (error) {
      console.error('Error al validar el JWT:', error);
      this.limpiarSesion();

      if (!silencioso) {
        alert(
          error instanceof Error
            ? error.message
            : 'El token proporcionado no es válido.'
        );
      }
    }
  }

  cerrarSesion(): void {
    this.limpiarSesion();
  }

  private limpiarSesion(): void {
    this.tokenApi = '';
    this.sesionActiva = false;
    this.esAdmin = false;

    this.activos = [];
    this.activosFiltrados = [];
    this.categorias = [];

    this.mostrarFormulario = false;
    this.esEdicion = false;
    this.activoEditandoId = null;
    this.guardando = false;
    this.exportando = false;
    this.nuevo = this.limpiarFormulario();
  }

  private convertirActivo(datos: any): Activo {
    return {
      id: String(datos.id),
      folio: datos.folioInventario,
      serie: datos.numeroSerie,
      marca: datos.marcaModelo,
      categoria: datos.categoriaNombre,
      estado: datos.estado,
      costo: Number(datos.costoAdquisicion)
    };
  }

  contarEstado(estado: string): number {
    return this.activos.filter(
      activo => this.normalizarEstado(activo.estado) === estado
    ).length;
  }

  private normalizarEstado(estado: string): string {
    return String(estado ?? '')
      .trim()
      .toUpperCase()
      .replace(/[\s-]+/g, '_');
  }

  buscar(): void {
    const serie = this.filtroSerie.trim().toLowerCase();
    const marca = this.filtroMarca.trim().toLowerCase();
    const estado = this.filtroEstado.toUpperCase();

    this.activosFiltrados = this.activos.filter(activo => {
      const coincideSerie = activo.serie.toLowerCase().includes(serie);
      const coincideMarca = activo.marca.toLowerCase().includes(marca);
      const coincideEstado = !estado ||
        this.normalizarEstado(activo.estado) === estado;

      return coincideSerie && coincideMarca && coincideEstado;
    });
  }

  cambiarEstado(activo: Activo, nuevoEstado: string): void {
    if (!this.sesionActiva || !this.tokenApi || !this.esAdmin) {
      alert('Solo el perfil ADMIN puede cambiar el estado.');
      this.buscar();
      return;
    }

    const estadoActual = this.normalizarEstado(activo.estado);
    const estadoNuevo = this.normalizarEstado(nuevoEstado);

    const estadosPermitidos = [
      'DISPONIBLE',
      'ASIGNADO',
      'EN_MANTENIMIENTO',
      'BAJA'
    ];

    if (!estadosPermitidos.includes(estadoNuevo)) {
      alert('El estado seleccionado no es válido.');
      this.buscar();
      return;
    }

    if (estadoActual === 'BAJA') {
      alert('Un activo dado de baja no puede cambiar de estado.');
      this.buscar();
      return;
    }

    if (estadoNuevo === estadoActual) {
      return;
    }

    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    const confirmar = confirm(
      `¿Deseas cambiar el estado del activo ${activo.folio} a ${this.textoEstado(estadoNuevo)}?`
    );

    if (!confirmar) {
      this.buscar();
      return;
    }

    this.http.patch<ActivoCreado>(
      `${this.apiUrl}/${activo.id}/estado?estado=${encodeURIComponent(estadoNuevo)}`,
      {},
      { headers: this.obtenerHeaders() }
    ).subscribe({
      next: (respuesta) => {
        this.activos = this.activos.map(item =>
          item.id === activo.id
            ? this.convertirActivo(respuesta)
            : item
        );

        this.buscar();

        alert(
          `Estado del activo ${activo.folio} actualizado a ${this.textoEstado(estadoNuevo)}.`
        );
      },
      error: (error) => {
        console.error('Error al cambiar el estado:', error);

        this.buscar();

        alert(
          error.status === 401
            ? 'Tu sesión expiró o el token no es válido.'
            : error.status === 403
              ? 'No tienes autorización para cambiar el estado.'
              : error.error?.message ||
                `No fue posible actualizar el estado. HTTP ${
                  error.status || 'sin respuesta'
                }.`
        );
      }
    });
  }

  limpiarFiltros(): void {
    this.filtroSerie = '';
    this.filtroMarca = '';
    this.filtroEstado = '';
    this.buscar();
  }

  claseEstado(estado: string): string {
    switch (this.normalizarEstado(estado)) {
      case 'DISPONIBLE':
        return 'status-disponible';
      case 'ASIGNADO':
        return 'status-asignado';
      case 'EN_MANTENIMIENTO':
        return 'status-mantenimiento';
      case 'BAJA':
        return 'status-baja';
      default:
        return '';
    }
  }

  textoEstado(estado: string): string {
    switch (this.normalizarEstado(estado)) {
      case 'DISPONIBLE':
        return 'Disponible';
      case 'ASIGNADO':
        return 'Asignado';
      case 'EN_MANTENIMIENTO':
        return 'En mantenimiento';
      case 'BAJA':
        return 'Baja';
      default:
        return estado;
    }
  }

  private limpiarFormulario(): ActivoRegistro {
    return {
      numeroSerie: '',
      marcaModelo: '',
      categoriaId: null,
      costoAdquisicion: null
    };
  }

  nuevoActivo(): void {
    if (!this.sesionActiva) {
      this.cambiarPerfil();
      return;
    }

    if (!this.esAdmin) {
      alert('Solo el perfil ADMIN puede registrar activos.');
      return;
    }

    this.esEdicion = false;
    this.activoEditandoId = null;
    this.nuevo = this.limpiarFormulario();

    this.cargarCategorias(() => {
      this.mostrarFormulario = true;
    });
  }

  private cargarCategorias(continuar?: () => void): void {
    if (!this.tokenApi || !this.sesionActiva) {
      alert('Ingresa un token válido antes de consultar categorías.');
      return;
    }

    this.http.get<Categoria[]>(this.categoriasUrl, {
      headers: this.obtenerHeaders()
    }).subscribe({
      next: (respuesta) => {
        this.categorias = respuesta ?? [];
        continuar?.();
      },
      error: (error) => {
        console.error('Error al cargar categorías:', error);
        alert(
          `No se pudieron cargar las categorías. HTTP ${
            error.status || 'sin respuesta'
          }.`
        );
      }
    });
  }

  guardarFormulario(): void {
    if (this.esEdicion) {
      this.guardarEdicion();
    } else {
      this.guardarActivo();
    }
  }

  guardarActivo(): void {
    if (!this.sesionActiva || !this.tokenApi) {
      this.cambiarPerfil();
      return;
    }

    if (!this.esAdmin) {
      alert('Solo el perfil ADMIN puede registrar activos.');
      return;
    }

    if (!this.validarFormulario()) {
      return;
    }

    this.guardando = true;

    this.http.post<ActivoCreado>(
      this.apiUrl,
      {
        numeroSerie: this.nuevo.numeroSerie.trim(),
        marcaModelo: this.nuevo.marcaModelo.trim(),
        categoriaId: this.nuevo.categoriaId,
        costoAdquisicion: this.nuevo.costoAdquisicion
      },
      { headers: this.obtenerHeaders() }
    ).subscribe({
      next: (respuesta) => {
        this.activos = [
          this.convertirActivo(respuesta),
          ...this.activos
        ];

        this.buscar();

        this.mostrarFormulario = false;
        this.esEdicion = false;
        this.activoEditandoId = null;
        this.nuevo = this.limpiarFormulario();

        alert(
          `¡Activo registrado con éxito!\nFolio: ${respuesta.folioInventario}`
        );
      },
      error: (error) => {
        console.error('Error al registrar activo:', error);
        this.guardando = false;

        alert(
          error.status === 401 || error.status === 403
            ? 'Tu sesión no está autorizada. Verifica el token y el rol.'
            : `No se pudo registrar el activo. HTTP ${
                error.status || 'sin respuesta'
              }.`
        );
      },
      complete: () => {
        this.guardando = false;
      }
    });
  }

  editarActivo(id: string): void {
    if (!this.sesionActiva) {
      this.cambiarPerfil();
      return;
    }

    if (!this.esAdmin) {
      alert('Solo el perfil ADMIN puede editar activos.');
      return;
    }

    const activo = this.activos.find(item => item.id === id);

    if (!activo) {
      alert('No se encontró el activo seleccionado.');
      return;
    }

    if (this.normalizarEstado(activo.estado) === 'BAJA') {
      alert('Un activo en estado BAJA no puede editarse.');
      return;
    }

    this.cargarCategorias(() => {
      this.esEdicion = true;
      this.activoEditandoId = id;

      const categoria = this.categorias.find(
        item => item.nombre === activo.categoria
      );

      this.nuevo = {
        numeroSerie: activo.serie,
        marcaModelo: activo.marca,
        categoriaId: categoria?.id ?? null,
        costoAdquisicion: activo.costo
      };

      this.mostrarFormulario = true;
    });
  }

  guardarEdicion(): void {
    if (!this.sesionActiva || !this.tokenApi) {
      this.cambiarPerfil();
      return;
    }

    if (!this.esAdmin) {
      alert('Solo el perfil ADMIN puede editar activos.');
      return;
    }

    if (!this.activoEditandoId) {
      alert('No se identificó el activo que deseas actualizar.');
      return;
    }

    const activoActual = this.activos.find(
      item => item.id === this.activoEditandoId
    );

    if (
      activoActual &&
      this.normalizarEstado(activoActual.estado) === 'BAJA'
    ) {
      alert('Un activo en estado BAJA no puede editarse.');
      return;
    }

    if (!this.validarFormulario()) {
      return;
    }

    this.guardando = true;

    this.http.put<ActivoCreado>(
      `${this.apiUrl}/${this.activoEditandoId}`,
      {
        numeroSerie: this.nuevo.numeroSerie.trim(),
        marcaModelo: this.nuevo.marcaModelo.trim(),
        categoriaId: this.nuevo.categoriaId,
        costoAdquisicion: this.nuevo.costoAdquisicion
      },
      { headers: this.obtenerHeaders() }
    ).subscribe({
      next: (respuesta) => {
        this.activos = this.activos.map(activo =>
          activo.id === this.activoEditandoId
            ? this.convertirActivo(respuesta)
            : activo
        );

        this.buscar();

        this.mostrarFormulario = false;
        this.esEdicion = false;
        this.activoEditandoId = null;
        this.nuevo = this.limpiarFormulario();

        alert('¡Activo actualizado con éxito!');
      },
      error: (error) => {
        console.error('Error al actualizar activo:', error);
        this.guardando = false;

        alert(
          error.status === 401 || error.status === 403
            ? 'Tu sesión no está autorizada. Verifica el token y el rol.'
            : `No se pudo actualizar el activo. HTTP ${
                error.status || 'sin respuesta'
              }.`
        );
      },
      complete: () => {
        this.guardando = false;
      }
    });
  }

  private validarFormulario(): boolean {
    if (
      !this.nuevo.numeroSerie.trim() ||
      !this.nuevo.marcaModelo.trim()
    ) {
      alert('Completa el número de serie y la marca/modelo.');
      return false;
    }

    if (this.nuevo.categoriaId === null) {
      alert('Selecciona una categoría.');
      return false;
    }

    if (
      this.nuevo.costoAdquisicion === null ||
      !Number.isFinite(Number(this.nuevo.costoAdquisicion)) ||
      Number(this.nuevo.costoAdquisicion) <= 0
    ) {
      alert('El costo de adquisición debe ser mayor que cero.');
      return false;
    }

    return true;
  }

  cancelarRegistro(): void {
    if (this.guardando) {
      return;
    }

    this.mostrarFormulario = false;
    this.esEdicion = false;
    this.activoEditandoId = null;
    this.nuevo = this.limpiarFormulario();
  }

  exportarReporte(): void {
    if (!this.sesionActiva || !this.tokenApi) {
      this.cambiarPerfil();
      return;
    }

    this.exportando = true;

    this.http.get<ReporteZipResponse>(
      `${this.apiUrl}/reportes/zip`,
      { headers: this.obtenerHeaders() }
    ).subscribe({
      next: (respuesta) => {
        try {
          const contenido = atob(respuesta.fileBase64);
          const bytes = new Uint8Array(contenido.length);

          for (let i = 0; i < contenido.length; i++) {
            bytes[i] = contenido.charCodeAt(i);
          }

          const archivo = new Blob(
            [bytes],
            { type: 'application/zip' }
          );

          const url = URL.createObjectURL(archivo);
          const enlace = document.createElement('a');

          enlace.href = url;
          enlace.download = respuesta.fileName || 'inventario.zip';
          document.body.appendChild(enlace);
          enlace.click();
          enlace.remove();

          setTimeout(() => URL.revokeObjectURL(url), 1000);

          alert('Reporte ZIP generado correctamente.');
        } catch (error) {
          console.error('Error al procesar el ZIP:', error);
          alert('No fue posible procesar el archivo ZIP.');
        }
      },
      error: (error) => {
        console.error('Error al exportar reporte:', error);

        alert(
          error.status === 401 || error.status === 403
            ? 'No tienes autorización para exportar el reporte.'
            : `No se pudo generar el reporte. HTTP ${
                error.status || 'sin respuesta'
              }.`
        );

        this.exportando = false;
      },
      complete: () => {
        this.exportando = false;
      }
    });
  }
}
