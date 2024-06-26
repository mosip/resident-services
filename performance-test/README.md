### Contains
* This folder contains scenario-based performance test scripts for Resident Services.
   
*List of  Scenarios:
	* UIN services Login
	* View My History 
	* Manage My VID
	* Secure My ID 
	* Track My Requests
	* Get Personalized Card
	* Share My Data
	* Update My Data
	* Get Information
	* Get My UIN
 	* Verify Phone/Email ID
	* Menu Bar
	* Audit
	* Logout
 
* Open source Tools used,
    1. [Apache JMeter](https://jmeter.apache.org/)

### How to run performance scripts using Apache JMeter tool
* Download Apache JMeter from https://jmeter.apache.org/download_jmeter.cgi
* Download scripts for the required module.
* Place the support files in the bin folder of the jmeter, the paths in the scripts are defined to fetch the testdata from the bin folder.
* Start JMeter by running the jmeter.bat file for Windows or jmeter file for Unix. 
* Validate the scripts for one user.
* Execute a dry run for 10 min.
* Execute performance run with various loads in order to achieve targeted NFR's.

### Setup points for Execution

* Create Identities in MOSIP Authentication System (Setup) : This thread contains the authorization api's for regproc and idrepo from which the auth token will be generated. There is set of 4 api's generate RID, generate UIN, add identity and add VID. From here we will get the VID's which can be further used as individual id for required Resident Service endpoints. These 4 api's are present in the loop controller where we can define the number of samples for creating identities in which "freshIdentityCreationCount" is used as a variable. In whichever environment we are running the scripts we should have atleast few hundred VID's available handy and if not we can use this setup to create the identities as required. 

* Resident Id Access Token Creation (Setup) : This thread contains 4 esignet api's Oauth details, Send OTP, Authentication, Authorization Code and 1 reisdent Login​ Redirect URI api.  After the login is successful, the resident will be redirected to the resident portal’s logged-in page. From Login​ Redirect URI api will get the id and access token which will be used further in the headers for most of the resident service api's. So till the time id and access token are valid and not expired we can re-use it for the resident service api's. And as per the expiration time once it is not valid and expired, we need to re-run the setup as required.

* For execution purpose need to modify the below mentioned properties: For Performance testing we require a specific amount of data which needs to be used further for the resident service api's and it should be valid till the time of execution. So, We have modified the below properties to increase the expiry time, so that the data prepared to be used for execution is valid until the execution is completed.

   * esignet default properties: Update the value for the properties according to the execution setup. Perform the execution for esignet api's with redis setup. So check for the redis setup accordingly.
          mosip.esignet.cache.size - Enabled while not using the redis setup. Can keep the cache size around more than 100k.
          mosip.esignet.cache.expire-in-seconds (authcodegenerated) - 21600
          mosip.esignet.access-token-expire-seconds - 86400
          mosip.esignet.id-token-expire-seconds - 86400
          spring.cache.type=redis - check for this property and enable the redis.
   * application default properties: Update the value for the below property.
          mosip.kernel.otp.expiry-time - 86400
   * id-authentication default properties: Update the value for the below properties.
          otp.request.flooding.max-count - 100000

* Create Resident Services EventId (Setup) : This thread contains service history endpoint api to capture the event id's required for the test.
          

### Data prerequisite

* List of VID's as per environment which is valid and will be prepared from the above mentioned create identities setup.
* List of Event id's as per environment which is valid and will be prepared from the above mentioned create resident services event id setup.

### Execution points for Resident Service endpoints

* The id and access token generated from the Login​ Redirect URI api will be stored in a file and will be used in the headers the api and can be re-used until they are not expired.
* Test duration is already defined as a variable and needs to be changed as per the test run duration requirement in the user defined variable section.
* The script is designed to run for 100 users, load being distributed accordingly for each thread group based on the weightage given for each scenario.


### Exact steps of execution

	Step 1: Enable only Create Identities in MOSIP Authentication System (Setup) thread group and toggle/disable the remaining thread groups in the script to create the required no of identities.
	Step 2: Enable only Resident Id Access Token Creation (Setup) thread group and toggle/disable the remaining thread groups in the script to create access tokens and id tokens.
	Step 3: Enable only Create Resident EventId (Setup) thread group and toggle/disable the remaining thread groups in the script to create the event id's.
	Step 4: Enable the rest of all the Execution based scenario thread groups and toggle/disable the first 3 setup based thread groups. 
	Step 5: Make sure test duration and ramp-up is defined in the user defined variable section. 
	Step 5: Click on Run/Excute the test.
	Step 6: Monitor the metrics during the test run using reports from jmeter, kibana, grafana and Jprofiler.
	
### Designing the workload model for performance test execution

* Calculation of number of users depending on Transactions per second (TPS) provided by client

* Applying little's law
	* Users = TPS * (SLA of transaction + think time + pacing)
	* TPS --> Transaction per second.

* For the realistic approach we can keep (Think time + Pacing) = 1 second for API testing
	* Calculating number of users for 10 TPS
		* Users= 10 X (SLA of transaction + 1)
		       = 10 X (1 + 1)
			   = 20
			   
### Usage of Constant Throughput timer to control Hits/sec from JMeter

* In order to control hits/ minute in JMeter, it is better to use Timer called Constant Throughput Timer.

* If we are performing load test with 10TPS as hits / sec in one thread group. Then we need to provide value hits / minute as in Constant Throughput Timer
	* Value = 10 X 60
			= 600

* Dropdown option in Constant Throughput Timer
	* Calculate Throughput based on as = All active threads in current thread group
		* If we are performing load test with 10TPS as hits / sec in one thread group. Then we need to provide value hits / minute as in Constant Throughput Timer
	 			Value = 10 X 60
					  = 600
		  
	* Calculate Throughput based on as = this thread
		* If we are performing scalability testing we need to calculate throughput for 10 TPS as 
          Value = (10 * 60 )/(Number of users)


### Description of the scenarios

* View My History: This feature enables the Resident to view the history of transactions associated with their UIN.

* Manage My VID: Residents can create, delete, and download VID cards based on requirements.

* Secure My ID: Residents can lock or unlock their authentication modalities such as fingerprint authentication, iris authentication, email OTP authentication, SMS OTP authentication, thumbprint authentication, and face authentication.

* Track My Requests: This feature enables the Residents to enter an Event ID associated with the logged-in user’s UIN to track the status of the event.

* Get Personalized Card: The residents can download a personalized card which essentially means that they can choose the attributes that they would want to be added to their cards.

* Share My Data: This feature enables Residents to choose the data that they want to share with a MOSIP-registered partner.

* Update My Data: This feature enables the Resident to update their identity data, address, email ID, phone number, and notification language preference.

* Logout: Once the Resident is done with the activities that he wanted to perform, he can end the active session by logging out from the portal.

* Get Information: Residents can get a list of Registration Centers near them or Registration Centers based on the location hierarchy also residents can get the list of all the supporting documents as Proof of Identity, Proof of Address, Proof of Relationship, etc.

* Get My UIN: Using this feature, the Resident can download their password-protected UIN card if the UIN card is ready or they can view the status of their Application ID (AID) if the UIN card is still under progress.

* Verify email ID and/ or phone number: Using this feature, the Resident can verify if the email ID/ Phone number given during registration is correct or not. This will be done by verifying the OTP sent over the registered email ID/ Phone number.

* Notifications: Residents will be getting bell-icon notifications for the asynchronous events if they have an active session i.e. they have logged into the Resident Portal.

* Profile details of the logged-in user (name, photo, and last login details): The Resident will be able to view the name, and photo of the logged-in user. They will also be able to see the last login details of the Resident.


