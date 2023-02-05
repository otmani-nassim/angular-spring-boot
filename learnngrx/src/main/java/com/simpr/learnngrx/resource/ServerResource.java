package com.simpr.learnngrx.resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.simpr.learnngrx.enumeration.Status;
import com.simpr.learnngrx.model.Response;
import com.simpr.learnngrx.model.Server;
import com.simpr.learnngrx.service.ServerServiceImp;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/server")
public class ServerResource {


    private ServerServiceImp serverServiceImp;

    public ServerResource(ServerServiceImp serverServiceImp) {
        this.serverServiceImp = serverServiceImp;
    }

    @GetMapping("/list")
    public ResponseEntity<Response> getServers(){
        return ResponseEntity.ok(
            Response.builder()
                .timeStamp(LocalDateTime.now())
                .data(Map.of("servers", serverServiceImp.list(10)))
                .message("server retrieved")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .build()
        );
    }

    @GetMapping("/ping/{ipAddress}")
    public ResponseEntity<Response> pingServer(@PathVariable("ipAddress") String ipAddress){
        Server server = serverServiceImp.ping(ipAddress);

        return ResponseEntity.ok(
            Response.builder()
                .timeStamp(LocalDateTime.now())
                .data(Map.of("server", server))
                .message(server.getStatus()==Status.SERVER_UP?"Ping Success": "Ping failed")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .build()
        );
    }

    @PostMapping("/save")
    public ResponseEntity<Response> saveServer(@RequestBody @Valid Server server){

        return ResponseEntity.ok(
            Response.builder()
                .timeStamp(LocalDateTime.now())
                .data(Map.of("server", serverServiceImp.create(server)))
                .message("server created")
                .status(HttpStatus.CREATED)
                .statusCode(HttpStatus.CREATED.value())
                .build()
        );
    }


    @GetMapping("/get/{id}")
    public ResponseEntity<Response> getServer(@PathVariable("id") Long id){
        return ResponseEntity.ok(
            Response.builder()
                .timeStamp(LocalDateTime.now())
                .data(Map.of("server", serverServiceImp.get(id)))
                .message("server retrieved")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .build()
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Response> deleteServer(@PathVariable("id") Long id){
        return ResponseEntity.ok(
            Response.builder()
                .timeStamp(LocalDateTime.now())
                .data(Map.of("deleted", serverServiceImp.delete(id)))
                .message("server deleted")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .build()
        );
    }


    @GetMapping(path="/image/{filename}",produces = MediaType.IMAGE_PNG_VALUE)

    public byte[] getServerImage(@PathVariable("filename") String filename) throws IOException{
        String path = System.getProperty("user.home")+"/Downloads/"+filename;
        
        return Files.readAllBytes(
            Paths.get(path)
            );
    }



}
