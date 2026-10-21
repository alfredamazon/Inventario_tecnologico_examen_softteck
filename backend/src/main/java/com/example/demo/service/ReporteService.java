package com.example.demo.service;

import com.example.demo.dto.ActivoResponseDTO;
import com.example.demo.dto.ReporteResponse;
import com.example.demo.entity.EstadoActivo;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ReporteService {

    private final ActivoService activoService;

    public ReporteService(ActivoService activoService) {
        this.activoService = activoService;
    }

    public ReporteResponse generar(
            String numeroSerie,
            String marcaModelo,
            Long categoriaId,
            EstadoActivo estado,
            BigDecimal costoMin,
            BigDecimal costoMax) throws IOException {

        List<ActivoResponseDTO> activos =
                activoService.buscarTodosConFiltros(
                        numeroSerie, marcaModelo, categoriaId,
                        estado, costoMin, costoMax);

        String usuario = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        LocalDateTime fecha = LocalDateTime.now();

        byte[] excel = generarExcel(activos);
        String auditoria = "Fecha de generación: " + fecha
                + "\nUsuario solicitante: " + usuario
                + "\nTotal exportado: " + activos.size();

        ByteArrayOutputStream salida = new ByteArrayOutputStream();

        try (ZipOutputStream zip = new ZipOutputStream(salida)) {
            zip.putNextEntry(new ZipEntry("activos.xlsx"));
            zip.write(excel);
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("auditoria.txt"));
            zip.write(auditoria.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            zip.closeEntry();
        }

        return new ReporteResponse(
                200,
                "Reporte generado correctamente",
                "inventario.zip",
                Base64.getEncoder().encodeToString(salida.toByteArray())
        );
    }

    private byte[] generarExcel(List<ActivoResponseDTO> activos)
            throws IOException {

        try (XSSFWorkbook libro = new XSSFWorkbook();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {

            Sheet hoja = libro.createSheet("Inventario");

            String[] columnas = {
                    "UUID", "Folio", "Número de serie", "Marca/Modelo",
                    "Estado", "Costo", "Fecha de ingreso", "Categoría"
            };

            Row encabezado = hoja.createRow(0);

            for (int i = 0; i < columnas.length; i++) {
                encabezado.createCell(i).setCellValue(columnas[i]);
            }

            for (int i = 0; i < activos.size(); i++) {
                ActivoResponseDTO activo = activos.get(i);
                Row fila = hoja.createRow(i + 1);

                fila.createCell(0).setCellValue(String.valueOf(activo.id()));
                fila.createCell(1).setCellValue(activo.folioInventario());
                fila.createCell(2).setCellValue(activo.numeroSerie());
                fila.createCell(3).setCellValue(activo.marcaModelo());
                fila.createCell(4).setCellValue(activo.estado());
                fila.createCell(5).setCellValue(
                        activo.costoAdquisicion().doubleValue());
                fila.createCell(6).setCellValue(
                        String.valueOf(activo.fechaIngreso()));
                fila.createCell(7).setCellValue(activo.categoriaNombre());
            }

            for (int i = 0; i < columnas.length; i++) {
                hoja.autoSizeColumn(i);
            }

            libro.write(salida);
            return salida.toByteArray();
        }
    }
}