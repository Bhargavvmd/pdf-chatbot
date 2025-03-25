package com.chatbot.controller;

import com.chatbot.service.PdfProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
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
@Tag(name = "PDF Management", description = "APIs for managing PDF documents")
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
    @Operation(
        summary = "Upload PDF file",
        description = "Uploads and processes a PDF file to extract Q&A pairs. " +
                     "The file is processed asynchronously, and the Q&A pairs are stored in the vector database.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "PDF processed successfully",
                content = @Content(mediaType = "text/plain")
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid file or empty file",
                content = @Content(mediaType = "text/plain")
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error during processing",
                content = @Content(mediaType = "text/plain")
            )
        }
    )
    @PostMapping("/upload")
    public CompletableFuture<ResponseEntity<String>> uploadPdf(
        @Parameter(
            description = "PDF file to process (must be a valid PDF file)",
            required = true,
            content = @Content(mediaType = "multipart/form-data")
        )
        @RequestParam("file") MultipartFile file
    ) {
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
    @Operation(
        summary = "Process PDF directory",
        description = "Processes all PDF files in a specified directory. " +
                     "This feature is not currently supported.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Directory processing status",
                content = @Content(mediaType = "text/plain")
            )
        }
    )
    @PostMapping("/process-directory")
    public CompletableFuture<ResponseEntity<String>> processDirectory(
        @Parameter(description = "Path to directory containing PDF files")
        @RequestParam String directoryPath
    ) {
        return CompletableFuture.completedFuture(
            ResponseEntity.ok("Directory processing is not supported in this version")
        );
    }

    @Operation(
        summary = "Handle exceptions",
        description = "Global exception handler for PDF operations",
        responses = {
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = @Content(mediaType = "text/plain")
            )
        }
    )
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.internalServerError()
            .body("An error occurred: " + e.getMessage());
    }
} 