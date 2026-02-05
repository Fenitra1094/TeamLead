package com.app.controllers;

import com.framework.annotation.*;
import com.framework.model.*;
import com.app.models.*;
import java.util.*;

@Controller
public class UploadController {
    
    // 1. Page d'accueil des tests upload
    @HandleGet("/upload-test")
    public ModelView uploadTestHome() {
        ModelView mv = new ModelView();
        mv.setView("upload-test-home.jsp");
        return mv;
    }
    
    // 2. Test 1: Upload simple
    @HandlePost("/upload-test/simple")
    public ModelView testSimpleUpload(@FileUpload("monfichier") UploadedFile fichier) {
        ModelView mv = new ModelView();
        mv.setView("../upload-test-result1.jsp");
        mv.addAttribute("fichier", fichier);
        return mv;
    }
    
    // 3. Test 2: Upload avec paramètres
    @HandlePost("/upload-test/with-params")
    public ModelView testUploadWithParams(
            @RequestParam("username") String username,
            @RequestParam("age") int age,
            @FileUpload("avatar") UploadedFile avatar) {
        
        ModelView mv = new ModelView();
        mv.setView("../upload-test-result2.jsp");
        mv.addAttribute("username", username);
        mv.addAttribute("age", age);
        mv.addAttribute("avatar", avatar);
        return mv;
    }
    
    // 4. Test 3: Upload multiple
    @HandlePost("/upload-test/multiple")
    public ModelView testMultipleUpload(@FileUpload("photos") UploadedFile[] photos) {
        ModelView mv = new ModelView();
        mv.setView("../upload-test-result3.jsp");
        mv.addAttribute("photos", photos);
        
        int count = 0;
        if (photos != null) {
            for (UploadedFile photo : photos) {
                if (photo != null && !photo.isEmpty()) count++;
            }
        }
        mv.addAttribute("photoCount", count);
        
        return mv;
    }
    
    // 5. Test 4: Upload avec objet User
    @HandlePost("/upload-test/with-user")
    public ModelView testUploadWithUser(
            User user,
            @FileUpload("document") UploadedFile document) {
        
        ModelView mv = new ModelView();
        mv.setView("../upload-test-result4.jsp");
        mv.addAttribute("user", user);
        mv.addAttribute("document", document);
        return mv;
    }
    
    // 6. Test 5: Upload avec Map
    @HandlePost("/upload-test/with-map")
    public ModelView testUploadWithMap(
            Map<String, Object> data,
            @FileUpload("fichier") UploadedFile fichier) {
        
        ModelView mv = new ModelView();
        mv.setView("../upload-test-result5.jsp");
        mv.addAttribute("data", data);
        mv.addAttribute("fichier", fichier);
        return mv;
    }
    
    // 7. Test 6: Upload mixte complet
    @HandlePost("/upload-test/complete")
    public ModelView testCompleteUpload(
            @RequestParam("titre") String titre,
            User user,
            @FileUpload("cv") UploadedFile cv,
            @FileUpload("certificats") UploadedFile[] certificats,
            Map<String, Object> extra) {
        
        ModelView mv = new ModelView();
        mv.setView("../upload-test-result6.jsp");
        mv.addAttribute("titre", titre);
        mv.addAttribute("user", user);
        mv.addAttribute("cv", cv);
        mv.addAttribute("certificats", certificats);
        mv.addAttribute("extra", extra);
        return mv;
    }
    
    // 8. Test 7: Upload avec JSON response
    @HandlePost("/upload-test/json")
    @JsonResponse
    public Map<String, Object> testUploadJson(
            @RequestParam("nom") String nom,
            @FileUpload("fichier") UploadedFile fichier) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("test", "../upload-json");
        response.put("nom", nom);
        
        if (fichier != null && !fichier.isEmpty()) {
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("fileName", fichier.getFileName());
            fileInfo.put("fileSize", fichier.getSize());
            fileInfo.put("contentType", fichier.getContentType());
            response.put("fichier", fileInfo);
        }
        
        response.put("success", true);
        return response;
    }
}