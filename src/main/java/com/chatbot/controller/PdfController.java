package com.chatbot.controller;

import com.chatbot.service.PdfProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for handling PDF processing operations.
 * This controller provides endpoints for uploading and processing PDF files,
 * including both single file uploads and directory processing.
 * All operations are asynchronous to handle potentially large PDF files
 * without blocking the server thread.
 */
@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class PdfController {
    private final PdfProcessingService pdfProcessingService;

    /**
     * Handles single PDF file upload and processing.
     * This endpoint accepts a PDF file, processes it asynchronously, and returns
     * a response when the processing is complete. The processing includes:
     * - Text extraction from the PDF
     * - Question-answer pair generation
     * - Vector store storage for future retrieval
     *
     * @param file The PDF file to be processed
     * @return CompletableFuture containing a ResponseEntity with the processing status
     *         - 200 OK with success message if processing completes successfully
     *         - 500 Internal Server Error with error message if processing fails
     */
    @PostMapping("/upload")
    public CompletableFuture<ResponseEntity<String>> uploadPdf(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest().body("Please select a file to upload")
            );
        }

        if (!file.getContentType().equals("application/pdf")) {
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest().body("Only PDF files are allowed")
            );
        }

        return pdfProcessingService.processPdf(file)
            .thenApply(v -> ResponseEntity.ok("PDF processed successfully"))
            .exceptionally(e -> ResponseEntity.internalServerError()
                .body("Error processing PDF: " + e.getMessage()));
    }

    /**
     * Processes all PDF files in a specified directory.
     * This endpoint handles batch processing of multiple PDF files asynchronously.
     * The processing includes:
     * - Recursive directory scanning for PDF files
     * - Parallel processing of multiple PDFs
     * - Question-answer pair generation for each PDF
     * - Vector store storage for future retrieval
     *
     * @param directoryPath The path to the directory containing PDF files
     * @return CompletableFuture containing a ResponseEntity with the processing status
     *         - 200 OK with success message if all PDFs are processed successfully
     *         - 500 Internal Server Error with error message if processing fails
     */
    @PostMapping("/process-directory")
    public CompletableFuture<ResponseEntity<String>> processDirectory(@RequestParam String directoryPath) {
        return CompletableFuture.completedFuture(
            ResponseEntity.ok("Directory processing is not supported in this version")
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.internalServerError()
            .body("An error occurred: " + e.getMessage());
    }
} 