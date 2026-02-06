package com.cousin.controller;

import com.framework.annotation.Controller;
import com.framework.annotation.GetMapping;
import com.framework.annotation.PostMapping;
import com.framework.annotation.FileUpload;
import com.framework.annotation.Json;
import com.framework.model.UploadedFile;
import jakarta.servlet.http.Part;

@Controller
public class UploadTestController {

    @GetMapping("/upload/form")
    public String showForm() {
        return """
            <h1>Upload Test</h1>
            <form method=\"POST\" action=\"/test/upload/save\" enctype=\"multipart/form-data\">
                <label>Fichier unique: <input type=\"file\" name=\"fichier\"></label><br>
                <label>Plusieurs fichiers: <input type=\"file\" name=\"documents\" multiple></label><br>
                <button>Envoyer</button>
            </form>
            """;
    }

    @PostMapping("/upload/save")
    public String save(@FileUpload("fichier") UploadedFile fichier, @FileUpload("documents") UploadedFile[] documents) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Résultat Upload</h1>");
        sb.append("<p>Fichier unique: " + (fichier != null ? fichier.toString() : "aucun") + "</p>");
        sb.append("<h2>Documents:</h2>");
        if (documents != null) {
            for (UploadedFile d : documents) {
                sb.append("<p> - " + d + "</p>");
            }
        } else {
            sb.append("<p>Aucun document</p>");
        }
        return sb.toString();
    }
}
