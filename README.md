Chit-Chat (Backend)

The purpose of this application is to construct the proper REST API for our React Frontend to interact with. This application works in conjunction with Chit-Chat's React Frontend (https://github.com/KBavis/chat-app-client.git) to allow users to register accounts, generate conversations with other users, and then chat with one and other. The backend utilizes Spring Boot to establish the necessary Entities, Controllers, and Services needed to faciliate our application. Our Spring Boot application works with our Postgres Database to save all messages, conversations, and users. On top of this, the Spring Boot application utilizes Kafka and Zookeeper for a high-throughput system in the context of real-time data processing. For real-time functionality, our application utilizes StompJS to generate Web Sockets for messages to be sent through, allowing users to interact in real-time. Finally, in order to provide some authentication, our Spring Boot application utilizes JWT Authenication Tokens to autherize our users and ensure that they are logging in correctly.

Working with Spring is an area I always wanted to delve into deeply. Backend development has always been my prefereence when it comes to web develoment work, so getting my hands dirty for this project was a great learning experience. With that being said, there was tons of challenges that I faced along the way that proved to be great learning tools. 1) Handling Entities. Find a way to establish the Many-To-Many, Many-To-One, and other relationships between Spring Entites was a learning experience. Understanding how these entities needed to utilize each other to establish a conneciton caused for some issues, especially when utilizing my Controllers in the fashion I originally was. The utilization of DTO's helped me establish a deeper knowledge of serialization and de-serialization of our entities and their corersponding relationships. 2) JWT Authentication. Learning how JWT works and how to leverage this for authentication was a really satisfying learning experieince. It caused a large amount of hiccups along the way, but I believe that my understanding has exceeded all the struggles! 3) Kafka. I really wanted to delve into what makes Kafka so great, and I don't entirely beleive my approach is as optimized as it possibly could have been. It was a struggle to understand the funadmentals and how to intertwine these with Spring, but after some thorough research, I feel a lot more confident about the topic. I just feel as though the way I utilize this service could be used more efficiently.

In order to install the application and run the project, please do the following:



-----------BACKEND SETUP--------------



		1) Clone The API repoistory (https://github.com/KBavis/chat-app-api.git) to your local machine.

		2) Update your local copy of the API as follows :

	       		 - Create an application.properties (see the template application.propeties file for example set-up)

	        	  - Run 'mvn clean install -DskipTests' to generate the .jar file needed


		3) Build your API Docker Image

		        - Ensure that the 'Dockerfile' is referencing the correct location of your .jar file and the proper name

			- Run the command 'docker build -t api-image:latest .' while in the working directy of your Docker Image and Target Directory (where your .jar should be located)


	4) Update Your Docker Compose File

		- Update KAFKA_ZOOKEEPER_CONNECT host domain (this should be the IPV4 Address of the 'bridge' network --> run 'docker inspect network bridge' to determine
		- Update KAFKA_ADVERTISED_LISTENERS host domain (if running locally, this should be localhost)



	5) Run Docker-Compose File

		- Execute the command 'docker-compose up -d' in the working directory of your docker-compose.yml file



	----------FRONTEND SETUP---------------



	1) Clone the Frontend Repository (https://github.com/KBavis/chat-app-client.git) to your local machine.



	2) Update conifg.js host domain to be 'localhost'



	3) If attempting to run locally, there is no need to utilize the dokcer-compose.yml file (as this serves as a reverse proxy for our EC2 Instance), so simply build your docker image via the following commnad:

		- docker build -t client-image:latest .



	4) Run the docker image :

		'docker run -p 3000:3000 client-image:latest'



	5) Access the login page by going to http://localhost:3000


TODO:
	1) Handling Entities - I think that the way I have embedded all necessary attributes into the JSON returned by certain GET requests was a bit foolish. I beli		eve that a better approach would have been to simply include the ID of the embedded entity, rather than all of its field and attributes. This is because i	     n the long run, this is going to cause some issues with scalability. On top of this, when a user sends hundreds or thousands of messages, that GET request 	  will be smothered with data, making it hard to interpret.
	2) Kafka - As mentioned above, I think my implementation of Kafka is a bit counter productive. I would love to be able to look into how to configure this for 		optimizing the real-time functionlaity this application provides. I feel as though another application using Kafka will help cement the concept for me.
	3) Test Cases - I believe I wrote some extremely sound test cases, but I think there is room for improvement. Being able to ensure that the application works 		as expected is largely determined by the effectiveness of my test cases. I think that in the future, I should consider how to strucutre these better
