package file.storage.demo.controller;

import file.storage.demo.service.FileStorageService;
import file.storage.demo.util.FileResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value="/api/fileManager")
public class FileManagerController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping(value = "/upload")
    public FileResponse uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);
        String fileUri = ServletUriComponentsBuilder.fromCurrentContextPath() // localhost:8080
                .path("api/fileManager/view") // localhost:8080/api/fileManager/view
                .path(fileName) // localhost:8080/api/fileManage/view/fileName.jpg
                .toUriString();
        return new FileResponse(fileName, fileUri, file.getContentType(), file.getSize());
    }

    @PostMapping(value = "/uploads")
    public List<FileResponse> uploadFiles(@RequestParam("files") MultipartFile[] files){
        return Arrays.asList(files) // chuyen mang files thanh list file
                .stream()   // chuyen list sang stream
                .map(file -> uploadFile(file))  // tai moi phan tu cua file thi thuc hien phuong thuc uploadFile(file)
                .collect(Collectors.toList());  // stream -> List
    }

    @GetMapping(value = "/view/{fileName:.+}")
    public ResponseEntity<?> viewFile(@PathVariable String fileName, HttpServletRequest httpServletRequest) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        String contentType = null;
        try{
            contentType = httpServletRequest.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch(IOException e) {
            e.printStackTrace();
        }
        if(contentType == null)
            contentType = "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
