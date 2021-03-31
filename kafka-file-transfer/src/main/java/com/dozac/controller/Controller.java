package com.dozac.controller;


import com.dozac.producer.ProducerKafka;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@RestController
@RequestMapping("/kafka")
public class Controller {

    @Autowired
    Environment environment;

    @Autowired
    ProducerKafka producer;


    @GetMapping("/sound")
    public ResponseEntity<ByteArrayResource> sound(@RequestParam("file") MultipartFile multipart) throws IOException {

        File target = convertToMp3(convertMultiPartToFile(multipart));
        HttpHeaders headers = new HttpHeaders();

        Path path = Paths.get(target.getAbsolutePath());
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
        byte[] audioByte = Files.readAllBytes(path);
        producer.sendMessage("audio data", audioByte);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(target.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private File convertMultiPartToFile(MultipartFile file) {
        try {
            File convFile = new File(file.getOriginalFilename());
            convFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(file.getBytes());
            fos.close();
            return convFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public File convertToMp3(File uploadedFile){
        boolean succeeded;
        try {
            File source = uploadedFile;

            //TODO add timestamps to file?
            File target = new File("/home/cnb/file.wav");

            //Audio Attributes
            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("libmp3lame");
            audio.setBitRate(128000);
            audio.setChannels(2);
            audio.setSamplingRate(44100);

            //Encoding attributes
            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setOutputFormat("wav");
            attrs.setAudioAttributes(audio);

            //Encode
            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(source), target, attrs);

            return target;

        } catch (Exception ex) {
            ex.printStackTrace();
            succeeded = false;
        }
        return uploadedFile;
    }


}
