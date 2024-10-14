### Contains
* This folder contains scenario-based performance test scripts for Resident Services.
   
*List of  Scenarios: 
	* S01 UIN Services Login
	* S02 View My History 
	* S03 Manage My VID
	* S04 Secure My ID 
	* S05 Track My Requests
	* S06 Get Personalized Card
	* S07 Share My Data
	* S08 Update My Data
	* S09 Menu Bar
	* S10 Profile ICon and Logout
	* S11 Get My UIN
	* S12 Get Information
	* S13 Audit
 	* S14 Verify Phone/Email ID

 
* Open source tools used,
    1. [Apache JMeter](https://jmeter.apache.org/)

### How to run performance scripts using Apache JMeter tool
* Download Apache JMeter from https://jmeter.apache.org/download_jmeter.cgi
* Download scripts for the required module.
* Place the support files in the bin folder of the jmeter, the paths in the scripts are defined to fetch the testdata from the bin folder.
* Start JMeter by running the jmeter.bat file for Windows or jmeter file for Unix. 
* Validate the scripts for one user.
* Execute a dry run for 10 min.
* Execute performance run with various loads in order to achieve targeted NFR's.

### Setup points before execution

* We need some jmeter plugin files that needs to be installed before opening of this script, PFA dependency links for your reference : 
	* jmeter-plugins-synthesis-2.2.jar
	* <!-- https://jmeter-plugins.org/files/packages/jpgc-synthesis-2.2.zip -->
	
Pre-requisite to install the plugins:
1. Download JMeter Plugin Manager jar file with following link. "https://jmeter-plugins.org/get/"
2. Place the jar file under following JMeter folder path.
3. After adding the plugin restart the JMeter 
4. To download the necessary plugins, we have to click on the Plugin’s Manager. It will re-direct to list of available plugins. Choose the above mentioned plugin "jmeter-plugins-synthesis" to install and then restart the JMeter.

 	   
* Create Identities in MOSIP Authentication System (Setup) : This thread contains the authorization api's for regproc and idrepo from which the auth token will be generated. There is set of 4 api's generate RID, generate UIN, add identity and add VID. From here we will get the VID's which can be further used as individual id for required Resident Service endpoints. These 4 api's are present in the loop controller where we can define the number of samples for creating identities in which "freshIdentityCreationCount" is used as a variable. In whichever environment we are running the scripts we should have atleast few hundred VID's available handy and if not we can use this setup to create the identities as required. 

* Resident Id Access Token Creation (Setup) : This thread contains 4 esignet api's Oauth details, Send OTP, Authentication, Authorization Code and 1 resident Login​ Redirect URI api.  After the login is successful, the resident will be redirected to the resident portal’s logged-in page. From Login​ Redirect URI api will get the id and access token which will be used further in the headers for most of the resident service api's. So till the time id and access token are valid and not expired we can re-use it for the resident service api's. And as per the expiration time once it is not valid and expired, we need to re-run the setup as required.

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

* Resident Id Access Token Creation For Logout(Preparation) : We need to create the resident id access tokens seperately for the logout scenario, because we cannot re use the same tokens that are generated for the other scenario's. As the tokens will be expired as soon as the logout is executed. Based on the TPS provided for logout scenario, we can keep the access tokens ready for the test.
          

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
* The script and the below calculation is preconfigured as per 100 tps, if you are testing for other tps, the below values needs to be adjusted.

* Applying little's law
	* Users = TPS * (SLA of transaction + think time + pacing)
	* TPS --> Transaction per second.

* For the realistic approach we can keep (Think time + Pacing) = 1 second for API testing
	* Calculating number of users for 100 TPS
		* Users= 100 X (SLA of transaction + 1)
		       = 100 X (1 + 1)
			   = 200
			   
### Usage of Constant Throughput timer to control Hits/sec from JMeter

* In order to control hits/ minute in JMeter, it is better to use Timer called Constant Throughput Timer. This is calculated explicitly for each thread group based on the scenario's weightage.

* If we are performing load test with 10TPS as hits / sec in one thread group/scenario. Then we need to provide value hits / minute as in Constant Throughput Timer
	* Value = 100 X 60
			= 6000

* Dropdown option in Constant Throughput Timer
	* Calculate Throughput based on as = All active threads in current thread group
		* If we are performing load test with 10TPS as hits / sec in one thread group. Then we need to provide value hits / minute as in Constant Throughput Timer
	 			Value = 10 X 60
					  = 600
		  
	* Calculate Throughput based on as = this thread
		* If we are performing scalability testing we need to calculate throughput for 10 TPS as 
          Value = (10 * 60 )/(Number of users)


### Description of the scenario's

* S01 UIN Services Login: This scenario includes login api's like V2 and redirect login via E-signet. Validate token endpoint API is also part of this scenario.

* S02 View My History: This feature enables the Resident to view the history of transactions associated with their UIN.

* S03 Manage My VID: Residents can create, delete, and download VID cards based on requirements.

* S04 Secure My ID: Residents can lock or unlock their authentication modalities such as fingerprint authentication, iris authentication, email OTP authentication, SMS OTP authentication, thumbprint authentication, and face authentication.

* S05 Track My Requests: This feature enables the Residents to enter an Event ID associated with the logged-in user’s UIN to track the status of the event.

* S06 Get Personalized Card: The residents can download a personalized card which essentially means that they can choose the attributes that they would want to be added to their cards.

* S07 Share My Data: This feature enables Residents to choose the data that they want to share with a MOSIP-registered partner.

* S08 Update My Data: This feature enables the Resident to update their identity data, address, email ID, phone number, and notification language preference.

* S09 Menu Bar: In the menu bar there are options like read, unread notifications, bell update time and also the language code.

* S10 Profile and Logout: The Resident will be able to view the name, and photo of the logged-in user. They will also be able to see the last login details of the Resident. In logout Once the Resident is done with the activities that he wanted to perform, he can end the active session by logging out from the portal.

* S11 Get My UIN: Using this feature, the Resident can download their password-protected UIN card if the UIN card is ready or they can view the status of their Application ID (AID) if the UIN card is still under progress.

* S12 Get Information: Residents can get a list of Registration Centers near them or Registration Centers based on the location hierarchy also residents can get the list of all the supporting documents as Proof of Identity, Proof of Address, Proof of Relationship, etc.

* S13 Audit: All the user activity is logged using the audit API's.

* S14 Verify email ID and/ or phone number: Using this feature, the Resident can verify if the email ID/ Phone number given during registration is correct or not. This will be done by verifying the OTP sent over the registered email ID/ Phone number.