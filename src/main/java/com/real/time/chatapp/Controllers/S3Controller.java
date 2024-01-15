package com.real.time.chatapp.Controllers;

import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.real.time.chatapp.ControllerServices.S3Service;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class S3Controller {
	private static Logger log = LoggerFactory.getLogger(S3Controller.class);
	private final S3Service service;
	
	/**
	 * Upload File to AWS Bucket
	 * 
	 * @param file - file to upload
	 * @return
	 */
	@PostMapping("/upload")
	public ResponseEntity<?> uploadFile(@RequestParam(value = "file") MultipartFile file) {
		return new ResponseEntity<>(service.uploadFile(file), HttpStatus.OK);
	}
	
	/**
	 * Download File From AWS Bucket
	 * 
	 * @param fileName - file to download
	 * @return
	 */
	@GetMapping("/download/{fileName}")
	public ResponseEntity<?> downloadFile(@PathVariable String fileName){
		byte[] data = service.downloadFile(fileName);
		ByteArrayResource resource = new ByteArrayResource(data);
		return ResponseEntity
				.ok()
				.contentLength(data.length)
				.header("Content-Type", "application/octet-stream")
				.header("Content-disposition", "attachment; filename=\"" + fileName + "\"")
				.body(resource);
	}
	
	/**
	 * Delete File From AWS Bucket
	 * 
	 * @param fileName - file to delete
	 * @return
	 */
	@DeleteMapping("/delete/{fileName}")
	public ResponseEntity<?> deleteFile(@PathVariable String fileName){
		return new ResponseEntity<>(service.deleteFile(fileName), HttpStatus.OK);
	}
}
