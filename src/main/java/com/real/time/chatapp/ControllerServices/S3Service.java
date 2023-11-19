package com.real.time.chatapp.ControllerServices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class S3Service {
	
	private static Logger log = LoggerFactory.getLogger(S3Service.class);
	private final AmazonS3 s3Client;
	private final UserService userService;
	private final UserRepository userRepository;
	
	@Value("${application.bucket.name}")
	private String bucketName;
	
	/**
	 * Function to upload file to AWS Bucket
	 * 
	 * @param file - file to be uploaded
	 * @return - success indication
	 */
	public String uploadFile(MultipartFile file) {
		System.out.println("In Upload File Service");
		File uploadFile = convertMultiPartFileToFile(file);
		String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
		s3Client.putObject(new PutObjectRequest(bucketName, fileName, uploadFile));
		//Remove File After Uploading To S3 Bucket
		uploadFile.delete();
		
		//Set Profile Image Of Authenticated User To Uploaded Image
		User user = userService.loadUser();
		user.setProfileImage("https://" + bucketName + ".s3.amazonaws.com/" + fileName);
		userRepository.save(user);
		
		return user.getProfileImage();
	}
	
	
	/**
	 * Function to download a file from our AWS Bucket 
	 * 
	 * @param fileName - filename to download
	 * @return - content of file
	 */
	public byte[] downloadFile(String fileName) {
		S3Object s3Object = s3Client.getObject(bucketName, fileName);
		S3ObjectInputStream inputStream = s3Object.getObjectContent();
		try {
			byte[] content = IOUtils.toByteArray(inputStream);
			return content;
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Function to delete a file from our AWS Bucket
	 * 
	 * @param fileName - file to be deleted
	 * @return - succesful indication
	 */
	public String deleteFile(String fileName) {
		s3Client.deleteObject(bucketName, fileName);
		return "File successfully deleted: " + fileName;
	}
	
	
	/**
	 * Method To Convert a MultiPart file to a File Object
	 * @param file
	 * @return
	 */
	private File convertMultiPartFileToFile(MultipartFile file) {
		File convertedFile = new File(file.getOriginalFilename());
		try (FileOutputStream fos = new FileOutputStream(convertedFile)){
			fos.write(file.getBytes());
		} catch(IOException e) {
			log.error("Error Converting MultipartFile to File", e);
		}
		return convertedFile;
	}
}
