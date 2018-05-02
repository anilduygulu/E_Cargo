<?php

class DbHandler {

    private $conn;

    function __construct() {
        require_once dirname(__FILE__) . '/DbConnect.php';
        // opening db connection
        $db = new DbConnect();
        $this->conn = $db->connect();
    }

    /* ------------- `users` table method ------------------ */

    /**
     * Creating new user
     * @param String $name User full name
     * @param String $email User login email id
     * @param String $password User login password
     */
    public function createUser($UserName, $UserSurname, $UserEmail, $UserPassword, $UserSex, $UserTcNo, $UserPhoneNumber, $UserType, $FcmToken) {
        require_once 'PassHash.php';
        $response = array();

        // First check if user already existed in db
        if (!$this->isUserExists($UserEmail)) {
            // Generating password hash
            $password_hash = PassHash::hash($UserPassword);

            // Generating API key
            $api_key = $this->generateApiKey();
            //Email token generated
            $EmailToken = md5(uniqid(rand()));
            
            //mail verification
            //$isEmailVerified = 0;
            
            // insert query
            $stmt = $this->conn->prepare("INSERT INTO user(UserName,UserSurname, UserMail, UserPassword, UserSex, UserTcNo, UserPhoneNumber, UserType, api_key, status, EmailToken, isEmailVerified, UserFcmToken) values(?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, 0, ?)");
            $stmt->bind_param("sssssssssss", $UserName, $UserSurname, $UserEmail, $password_hash, $UserSex, $UserTcNo, $UserPhoneNumber, $UserType, $api_key, $EmailToken, $FcmToken);

            $result = $stmt->execute();

            $stmt->close();
            
            if (strcmp($UserType, "Customer") == 0 && $result) {
                $stmt = $this->conn->prepare("INSERT INTO customer(CustomerMail) values(?)");
                $stmt->bind_param("s", $UserEmail);
                $stmt->execute();
                $stmt->close();
            } else if (strcmp($UserType, "Carrier") == 0 && $result) {
                $stmt = $this->conn->prepare("INSERT INTO carrier(CarrierMail) values(?)");
                $stmt->bind_param("s", $UserEmail);
                $stmt->execute();
                $stmt->close();
            }

            // Check for successful insertion
            if ($result) {
                // User successfully inserted
                $this->sendVerificationMail($UserEmail, $EmailToken);
                return USER_CREATED_SUCCESSFULLY;
            } else {
                // Failed to create user
                return USER_CREATE_FAILED;
            }
        } else {
            // User with same email already existed in the db
            return USER_ALREADY_EXISTED;
        }

        return $response;
    }

    /**
     * Checking user login
     * @param String $email User login email id
     * @param String $password User login password
     * @return boolean User login status success/fail
     */
    public function checkLogin($UserMail, $UserPassword) {
        // fetching user by email
        $stmt = $this->conn->prepare("SELECT UserPassword FROM user WHERE UserMail = ? AND isEmailVerified = 1");
        $stmt->bind_param("s", $UserMail);
        $stmt->execute();
        $stmt->bind_result($password_hash);
        $stmt->store_result();

        if ($stmt->num_rows > 0) {
            // Found user with the email
            // Now verify the password
            $stmt->fetch();
            $stmt->close();

            if (PassHash::check_password($password_hash, $UserPassword)) {
                // User password is correct
                
                
                return TRUE;
            } else {
                // user password is incorrect
                return FALSE;
            }
        } else {
            $stmt->close();

            // user not existed with the email
            return FALSE;
        }
    }
    
    


    /**
     * Checking for duplicate user by email address
     * @param String $UserEmail email to check in db
     * @return boolean
     */
    private function isUserExists($UserEmail) {
        $stmt = $this->conn->prepare("SELECT UserID from user WHERE UserMail = ?");
        $stmt->bind_param("s", $UserEmail);
        $stmt->execute();
        $stmt->store_result();
        $num_rows = $stmt->num_rows;
        $stmt->close();
        return $num_rows > 0;
    }
    
    private function getMailToken($UserMail){
        $stmt = $this->conn->prepare("SELECT EMailToken from user where UserMail = ?");
        $stmt->bind_param("s", $UserMail);
        if($stmt->execute()){
           $stmt->bind_result($EmailToken);
           $stmt->fetch();
           $user = array();
           $user["MailToken"] = $EmailToken;
           
           $stmt->close();
           return $user;
       }else{
           return NULL;
       
        }
        
    }
    
    public function getCustomerID($UserMail){
        $stmt = $this->conn->prepare("SELECT CustomerID from customer where CustomerMail = ?");
        $stmt->bind_param("s", $UserMail);
       if($stmt->execute()){
           $stmt->bind_result($CustomerID);
           $stmt->fetch();
           $user = array();
           $user["CustomerID"] = $CustomerID;
           
           $stmt->close();
           return $user;
       }else{
           return NULL;
       
        }
    }

    public function getCarrierID($UserMail){
        $stmt = $this->conn->prepare("SELECT CarrierID from carrier where CarrierMail = ?");
        $stmt->bind_param("s", $UserMail);
       if($stmt->execute()){
           $stmt->bind_result($CarrierID);
           $stmt->fetch();
           $user = array();
           $user["CarrierID"] = $CarrierID;
           
           $stmt->close();
           return $user;
       }else{
           return NULL;
       
        }
    }
    /**
     * Fetching user by email
     * @param String $email User email id
     */
    public function getUserByEmail($email) {
        $stmt = $this->conn->prepare("SELECT UserName, UserSurname, UserType, UserMail, api_key, status FROM user WHERE UserMail = ?");
        $stmt->bind_param("s", $email);
        if ($stmt->execute()) {
            // $user = $stmt->get_result()->fetch_assoc();
            $stmt->bind_result($name, $surname, $customerType, $email, $api_key, $status);
            $stmt->fetch();
            $user = array();
            $user["UserName"] = $name;
            $user["UserSurname"] = $surname;
            $user["UserType"] = $customerType;
            $user["UserMail"] = $email;
            $user["api_key"] = $api_key;
            $user["status"] = $status;
            
            $stmt->close();
            
            if($customerType === "Customer"){
                $stmt = $this->conn->prepare("SELECT CustomerID FROM customer WHERE CustomerMail = ?");
                $stmt->bind_param("s",$email);
                $stmt->execute();
                $stmt->bind_result($CustomerID);
                $stmt->fetch();
                $user["CustomerID"] = $CustomerID;
            }else{
                $stmt = $this->conn->prepare("SELECT CarrierID FROM carrier WHERE CarrierMail = ?");
                $stmt->bind_param("s",$email);
                $stmt->execute();
                $stmt->bind_result($CarrierID);
                $stmt->fetch();
                $user["CarrierID"] = $CarrierID;
            }
            
            $stmt->close();
            return $user;
        } else {
            return NULL;
        }
    }

    /**
     * Fetching user api key
     * @param String $user_id user id primary key in user table
     */
    public function getApiKeyById($user_id) {
        $stmt = $this->conn->prepare("SELECT api_key FROM user WHERE UserID = ?");
        $stmt->bind_param("i", $user_id);
        if ($stmt->execute()) {
            // $api_key = $stmt->get_result()->fetch_assoc();
            // TODO
            $stmt->bind_result($api_key);
            $stmt->close();
            return $api_key;
        } else {
            return NULL;
        }
    }

    public function getIDByMail($UserMail) {
        $stmt = $this->conn->prepare("SELECT UserID FROM user WHERE UserMail = ?");
        $stmt->bind_param("s", $UserMail);
        $userId = $stmt->execute();
        $stmt->bind_result($userId);
        $stmt->fetch();
        $stmt->close();
        return $userId;
 
    }

    /**
     * Fetching user id by api key
     * @param String $api_key user api key
     */
    public function getUserId($api_key) {
        $stmt = $this->conn->prepare("SELECT UserID FROM user WHERE api_key = ?");
        $stmt->bind_param("s", $api_key);
        if ($stmt->execute()) {
            $stmt->bind_result($user_id);
            $stmt->fetch();
            // TODO
            // $user_id = $stmt->get_result()->fetch_assoc();
            $stmt->close();
            return $user_id;
        } else {
            return NULL;
        }
    }
    public function getCustomerIdByApiKey($api_key) {
        $stmt = $this->conn->prepare("SELECT CustomerID FROm customer WHERE CustomerMail = (SELECT UserMail FROM user WHERE api_key = ?)");
        $stmt->bind_param("s", $api_key);
        if ($stmt->execute()) {
            $stmt->bind_result($user_id);
            $stmt->fetch();
            // TODO
            // $user_id = $stmt->get_result()->fetch_assoc();
            $stmt->close();
            return $user_id;
        } else {
            return NULL;
        }
    }
    
        public function getCarrierIdByApiKey($api_key) {
        $stmt = $this->conn->prepare("SELECT CarrierID FROm carrier WHERE CarrierMail = (SELECT UserMail FROM user WHERE api_key = ?)");
        $stmt->bind_param("s", $api_key);
        if ($stmt->execute()) {
            $stmt->bind_result($user_id);
            $stmt->fetch();
            // TODO
            // $user_id = $stmt->get_result()->fetch_assoc();
            $stmt->close();
            return $user_id;
        } else {
            return NULL;
        }
    }
    
    public function getCustomerInfoByID($CustomerID){
        $stmt = $this->conn->prepare("SELECT UserName, UserSurname, UserPhoneNumber, UserMail FROM user WHERE UserMail = ( SELECT CustomerMail FROM customer WHERE CustomerID = ? )");
        $stmt->bind_param("i",$CustomerID);
        $stmt->execute();
        $customerinfo = $stmt->get_result();
        $stmt->close();
        return $customerinfo;
    }
        public function getCarrierInfoByID($CarrierID){
        $stmt = $this->conn->prepare("SELECT UserName, UserSurname, UserPhoneNumber, UserMail FROM user WHERE UserMail = ( SELECT CarrierMail FROM carrier WHERE CarrierID = ? )");
        $stmt->bind_param("i",$CarrierID);
        $stmt->execute();
        $carrierinfo = $stmt->get_result();
        $stmt->close();
        return $carrierinfo;
    }
    
    public function getCarrierIdByCarID($CarID){
        
        $stmt = $this->conn->prepare("SELECT CarrierID FROM car WHERE CarID = ?");
        $stmt->bind_param("i",$CarID);
        $stmt->execute();
        $userInfo = $stmt->get_result();
        $stmt->close();
        return $userInfo;
        

    }
    
  

    /**
     * Validating user api key
     * If the api key is there in db, it is a valid key
     * @param String $api_key user api key
     * @return boolean
     */
    public function isValidApiKey($api_key) {
        $stmt = $this->conn->prepare("SELECT UserID from user WHERE api_key = ?");
        $stmt->bind_param("s", $api_key);
        $stmt->execute();
        $stmt->store_result();
        $num_rows = $stmt->num_rows;
        $stmt->close();
        return $num_rows > 0;
    }

    /**
     * Generating random Unique MD5 String for user Api key
     */
    private function generateApiKey() {
        return md5(uniqid(rand(), true));
    }
    
    /* 
     * Update Fcm
     */
    
    public function updateFcm($UserMail, $FcmToken){

            $stmt = $this->conn->prepare("UPDATE user SET UserFcmToken = ? WHERE UserMail = ?");
            $stmt->bind_param("ss",$FcmToken,$UserMail);
            $stmt->execute();
            $num_affected_rows = $stmt->affected_rows;
            $stmt->close();
            echoRespnse(200, $num_affected_rows);
            if($num_affected_rows > 0){
                return $num_affected_rows;
            }else{
                return NULL;
            }
        
    }
    
    // ---- //

    /* Creating a product for Customer  */

    public function createProduct($CustomerID, $ProductName, $ProductFrom, $ProductTo, $ProductDetails) {
        
        $stmt = $this->conn->prepare("SELECT * FROM product WHERE CustomerID = ? AND ProductName = ? AND ProductFrom = ? AND ProductTo = ? AND ProductDetails = ?");
        $stmt->bind_param("sssss", $CustomerID, $ProductName, $ProductFrom, $ProductTo, $ProductDetails);
        $stmt->execute();
        $res = $stmt->get_result();
        $stmt->close();
        if($res->num_rows < 1){
            $stmt = $this->conn->prepare("INSERT INTO product(CustomerID, ProductName, ProductFrom, ProductTo, ProductDetails) values(? , ?, ?, ?, ?)");
            $stmt->bind_param("sssss", $CustomerID, $ProductName, $ProductFrom, $ProductTo, $ProductDetails);
            $result = $stmt->execute();
            $stmt->close();

            $new_product_id = $this->conn->insert_id;
            if ($result) {
                return $new_product_id;
            } else {
               return NULL;
            }
        }else{
            return false;
        }

    }

    /* ------------- `tasks` table method ------------------ */

    /**
     * Fetching single product
     * @param String $task_id id of the task
     */
    public function getProduct($product_id, $user_id) {
        $stmt = $this->conn->prepare("SELECT * FROM product WHERE CustomerID = ? AND ProductID = ?");
        $stmt->bind_param("ii", $user_id, $product_id);

        $stmt->execute();
        $productinfo = $stmt->get_result();
        $stmt->close();
        return $productinfo;
         
    }
    
    //Get All Products
        public function getAllProducts() {
        $stmt = $this->conn->prepare("SELECT * FROM `product` WHERE 1=1 AND NOT EXISTS ( SELECT * FROM service WHERE service.ProductID = product.ProductID)" );
        $stmt->execute();
        $products = $stmt->get_result();
        $stmt->close();
        return $products;
    }

    /**
     * Fetching all user products
     * @param String $user_id id of the user
     */
    public function getAllUserProducts($user_id) {
        $stmt = $this->conn->prepare("SELECT * FROM product WHERE CustomerID = ?");
        $stmt->bind_param("i", $user_id);
        $stmt->execute();
        $products = $stmt->get_result();
        $stmt->close();
        return $products;
    }
    
    public function getAllProductsByRouteID($RouteID) {
        $stmt = $this->conn->prepare("SELECT * FROM product WHERE ProductID = ANY (SELECT ProductID FROM service WHERE RouteID = ?) ");
        $stmt->bind_param("i", $RouteID);
        $stmt->execute();
        $products = $stmt->get_result();
        $stmt->close();
        return $products;
    }
    
    public function getAllProductsOnRouteByCarrierID($CarrierID){
        $stmt = $this->conn->prepare("SELECT DISTINCT * FROM product INNER JOIN route ON product.ProductID = ANY ( SELECT DISTINCT product.ProductID FROM product WHERE product.ProductID = ANY ( SELECT DISTINCT service.ProductID FROM route INNER JOIN service ON route.RouteID = ANY ( SELECT route.RouteID from car INNER JOIN route ON route.CarID = car.CarID WHERE car.CarrierID = ? ) GROUP BY service.ProductID ) ) GROUP BY product.ProductID, route.RouteID ");
        $stmt->bind_param("i", $CarrierID);
        $stmt->execute();
        $products = $stmt->get_result();
        $stmt->close();
        return $products;
    }

    /**
     * Updating product
     * @param String $task_id id of the task
     * @param String $task task text
     * @param String $status task status
     */
    public function updateProduct($CustomerID, $ProductName, $ProductFrom, $ProductTo, $ProductID, $ProductDetails) {
        $stmt = $this->conn->prepare("UPDATE product SET ProductName = ?, ProductFrom = ?, ProductTo = ?, ProductDetails = ? WHERE CustomerID = ? AND ProductID = ?");
        $stmt->bind_param("ssssss", $ProductName, $ProductFrom, $ProductTo, $CustomerID, $ProductID, $ProductDetails);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
    }

    /**
     * Deleting a product
     * @param String $task_id id of the task to delete
     */
    public function deleteProduct($ProductID) {
        $stmt = $this->conn->prepare("SELECT * FROM service WHERE ProductID = ?");
        $stmt->bind_param("i", $ProductID);
        $stmt->execute();
        $res = $stmt->get_result();
        $stmt->close();
        if($res->num_rows < 1){
            $stmt = $this->conn->prepare("DELETE FROM product WHERE ProductID = ?");
            $stmt->bind_param("i", $ProductID);
            $stmt->execute();
            
            if($stmt->execute()){
                $stmt->close();
                return true;
            }else{
                $stmt->close();
                return NULL;
            }
        }else{
            return false;
        }
        
    }

    /* ------------- `user_tasks` table method ------------------ */



    /* Creating a car for Carrier  */

    public function createCar($CarrierID, $CarPlate, $CarBrand, $CarModel) {
        
        $stmt = $this->conn->prepare("SELECT * FROM car WHERE CarrierID = ? AND CarPlate = ? AND CarBrand = ? AND CarModel = ?");
        $stmt->bind_param("ssss", $CarrierID, $CarPlate, $CarBrand, $CarModel);
        $stmt->execute();
        $res = $stmt->get_result();
        $stmt->close();
        if($res->num_rows < 1){
            $stmt = $this->conn->prepare("INSERT INTO car(CarrierID, CarPlate, CarBrand, CarModel) values(?, ?, ?, ?)");
            $stmt->bind_param("ssss", $CarrierID, $CarPlate, $CarBrand, $CarModel);
            $result = $stmt->execute();
            $stmt->close();
            $new_product_id = $this->conn->insert_id;
            if ($result) {
                return $new_product_id;
            } else {
                return NULL;
            }
        }else{
            return false;
        }
        

    }

    /**
     * Fetching single car
     * @param String $task_id id of the task
     */
    public function getCar($CarID, $CarrierID) {
        $stmt = $this->conn->prepare("SELECT * FROM car WHERE CarrierID = ? AND CarID = ?");
        $stmt->bind_param("ii", $CarrierID, $CarID);

        if ($stmt->execute()) {
            $res = array();
            $stmt->bind_result($CarID, $CarrierID, $CarPlate, $CarBrand, $CarModel);
            // TODO
            // $task = $stmt->get_result()->fetch_assoc();
            $stmt->fetch();
            $res["CarID"] = $CarID;
            $res["CarrierID"] = $CarrierID;
            $res["CarPlate"] = $CarPlate;
            $res["CarBrand"] = $CarBrand;
            $res["CarModel"] = $CarModel;
            $stmt->close();
            return $res;
        } else {
            return NULL;
        }
    }

    /**
     * Fetching all user cars
     * @param String $user_id id of the user
     */
    public function getAllUserCars($user_id) {
        $stmt = $this->conn->prepare("SELECT * FROM car WHERE CarrierID = ?");
        $stmt->bind_param("i", $user_id);
        $stmt->execute();
        $products = $stmt->get_result();
        $stmt->close();
        return $products;
    }

    /**
     * Updating car
     * @param String $task_id id of the task
     * @param String $task task text
     * @param String $status task status
     */
    public function updateCar($CarrierID, $CarPlate, $CarBrand, $CarModel, $CarID) {
        $stmt = $this->conn->prepare("UPDATE product SET CarPlate = ?, CarBrand = ?, CarModel = ? WHERE CarrierID = ? AND CarID = ?");
        $stmt->bind_param("sssss", $CarPlate, $CarBrand, $CarModel, $CarrierID, $CarID);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
    }

    /**
     * Deleting a car
     * @param String $task_id id of the task to delete
     */
    public function deleteCar($CarID) {
        
        $stmt = $this->conn->prepare("SELECT * FROM route WHERE CarID = ?");
        $stmt->bind_param("i", $CarID);
        $stmt->execute();
        $res = $stmt->get_result();
        $stmt->close();
        if($res->num_rows < 1){
            $stmt = $this->conn->prepare("DELETE FROM car WHERE CarID = ?");
            $stmt->bind_param("i", $CarID);
            $stmt->execute();
            
            if($stmt->execute()){
                $stmt->close();
                return true;
            }else{
                $stmt->close();
                return NULL;
            }
        }else{
            return false;
        }
        

    }
    /*-------------------*/
    /*
            Get User info by Car ID
     */
    
    public function getUserInfoByCarID($CarID){
        $stmt = $this->conn->prepare("SELECT UserName,UserSurname,UserPhoneNumber,UserMail FROM user WHERE UserMail = ( SELECT CarrierMail FROM carrier WHERE CarrierID = ( SELECT CarrierID from car WHERE CarID = ? ) )");
        $stmt->bind_param("i",$CarID);
        $stmt->execute();
        $userInfo = $stmt->get_result();
        $stmt->close();
        return $userInfo;
    }
    
    /*-------------------*/
    
    /*
     * Creeating Route
     * @param String $UserID
     * @param String $CarID
     */
    
    public function createRoute($CarID,$RouteFrom,$RouteTo, $RouteDate){

            $fulldate = explode(" ", str_replace('"', "", $RouteDate));
            $date = $fulldate[0];
        
            $stmt = $this->conn->prepare("SELECT * FROM route WHERE CarID = ? AND RouteFrom = ? AND RouteTo = ? AND RouteDate = ?");
            $stmt->bind_param("isss", $CarID, $RouteFrom, $RouteTo, $date);
            $stmt->execute();
            $res = $stmt->get_result();
        
        if($res->num_rows < 1){
            $stmt = $this->conn->prepare("INSERT INTO route(CarID, RouteFrom, RouteTo, RouteDate) values(?, ?, ?, ?)");
            $stmt->bind_param("isss", $CarID, $RouteFrom, $RouteTo, $RouteDate);

            if($stmt->execute()){
                $stmt->close();
                $new_notification_id = $this->conn->insert_id;
                return $new_notification_id;
            }else{
                $stmt->close();
                return NULL;
            }
        }else{
            $stmt->close();
            return false;
            }
        
        

        

    }
    
    public function getRoute($RouteID){
        $stmt = $this->conn->prepare("SELECT * FROM route WHERE RouteID = ?");
        $stmt->bind_param("s", $RouteID);
        $stmt->execute();
        $productinfo = $stmt->get_result();
        $stmt->close();
        return $productinfo;
    }
    
    public function searchRoutesByLocation($RouteFrom,$RouteTo){
        $stmt = $this->conn->prepare("SELECT CarID,RouteID FROM route WHERE RouteFrom = ? AND RouteTo = ?");
        $stmt->bind_param("ss",$RouteFrom,$RouteTo);
        $stmt->execute();
        $route = $stmt->get_result();
        $stmt->close();
        if($route->num_rows < 1){
            return FALSE;
        }else{
            return $route;
        }
    }
        public function listRoutesByCarID($CarID){
        $stmt = $this->conn->prepare("SELECT * FROM route WHERE CarID = (select CarID from car where CarPlate = ?)");
        $stmt->bind_param("s", $CarID);
        $stmt->execute();
        $routes = $stmt->get_result();
        $stmt->close();
        return $routes;
    }
    
    
    public function listAllRoutes(){
        $stmt = $this->conn->prepare("SELECT * FROM route");
        $stmt->execute();
        $routes = $stmt->get_result();
        $stmt->close();
        return $routes;
    }
    
    public function listRoutesByID($CarID){
        $stmt = $this->conn->prepare("SELECT * FROM route where CarID = ?");
        $stmt->bind_param("i",$CarID);
        $stmt->execute();
        $routes = $stmt->get_result();
        $stmt->close();
        return $routes;
    }
    
    public function listRoutesByCarrierID($CarrierID){
        $stmt = $this->conn->prepare("SELECT * FROM route WHERE CarID = (SELECT CarID FROM carrier WHERE CarrierID = ?)");
        $stmt->bind_param("i",$CarrierID);
        $stmt->execute();
        $routes = $stmt->get_result();
        $stmt->close();
        return $routes;
    }
    
    public function deleteRoute($RouteID) {
        $stmt = $this->conn->prepare("SELECT * FROM service WHERE RouteID = ?");
        $stmt->bind_param("i", $RouteID);
        $stmt->execute();
        $res = $stmt->get_result();
        $stmt->close();
        
        if($res->num_rows < 1){
            $stmt = $this->conn->prepare("DELETE FROM route WHERE RouteID = ?");
            $stmt->bind_param("i", $RouteID);
            $stmt->execute();
            if($stmt->execute()){
                $stmt->close();
                return true;
            }else{
                $stmt->close();
                return NULL;
            }
        }else{
            return false;
        }

    }
    
    public function updateRoute($RouteID, $RouteFrom, $RouteTo, $RouteDate, $CarID) {
        $stmt = $this->conn->prepare("UPDATE route SET RouteFrom = ?, RouteTo = ?, RouteDate = ? WHERE RouteID = ? AND CarID = ?");
        $stmt->bind_param("sssss", $RouteID, $RouteFrom, $RouteTo, $RouteDate, $CarID);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
    }
    
    // Notification methods
    //------//
    public function addNotificationCustomer($ProductID,$CarID,$CustomerID,$CarrierID, $RouteID){
        
        $stmt = $this->conn->prepare("SELECT * FROM notifications WHERE ProductID = ? AND CarID = ? AND SenderID = ? AND ReceiverID = ? AND RouteID = ?");
        $stmt->bind_param("iiiii", $ProductID, $CarID, $CustomerID, $CarrierID, $RouteID);
        $stmt->execute();
        $route = $stmt->get_result();
        $stmt->close();
        if($route->num_rows < 1){
                    $stmt = $this->conn->prepare("INSERT INTO notifications(ProductID, CarID, SenderID, ReceiverID, RouteID) values ( ?, ?, ?, ?, ? )");
                    $stmt->bind_param("sssss", $ProductID,$CarID,$CustomerID,$CarrierID,$RouteID);
                    $stmt->execute();
                    $stmt->close();
                    $new_notification_id = $this->conn->insert_id;
                    
                $stmt = $this->conn->prepare("SELECT UserFcmToken FROM user WHERE UserMail = (SELECT CarrierMail FROM carrier WHERE CarrierID = ?)");
                $stmt->bind_param("s",$CarrierID);
                $stmt->execute();
                $FcmToken = $stmt->get_result();
                $stmt->close();
                
                $UserFcmToken = array();
                while($fcm = $FcmToken->fetch_assoc()){
                    $UserFcmToken["UserFcmToken"] = $fcm["UserFcmToken"];
                }
                
                
                $stmt = $this->conn->prepare("SELECT UserName FROM user WHERE UserMail = ( SELECT CustomerMail FROM customer WHERE CustomerID = ? )");
                $stmt->bind_param("s",$CustomerID);
                $stmt->execute();
                $UserName = $stmt->get_result();
                $stmt->close();
                
                
                $UserNamee = array();
                while($fcm = $UserName->fetch_assoc()){
                    $UserNamee["UserName"] = $fcm["UserName"];
                }
                        
                $stmt = $this->conn->prepare("SELECT ProductName FROM product WHERE ProductID = ?");
                $stmt->bind_param("s",$ProductID);
                $stmt->execute();
                $ProductName = $stmt->get_result();
                $stmt->close();     
                
                
                $ProductNamee = array();
                while($fcm = $ProductName->fetch_assoc()){
                    $ProductNamee["ProductName"] = $fcm["ProductName"];
                }
                $Message = $UserNamee["UserName"]." adli kullanici ".$ProductNamee["ProductName"]." urununu tasitmak istiyor";
                $this->sendNotification(str_replace('"', '', $UserFcmToken["UserFcmToken"]), $Message);
            if ($route) {

               return $new_notification_id;
            } else {
               return NULL;
            }
        }else{
            return false;
        }  
    }
    
    public function addNotificationCarrier($ProductID,$CarID,$CarrierID,$CustomerID, $RouteID){
        
        $stmt = $this->conn->prepare("SELECT * FROM notifications WHERE ProductID = ? AND CarID = ? AND SenderID = ? AND ReceiverID = ? AND RouteID = ?");
        $stmt->bind_param("iiiii", $ProductID, $CarID, $CarrierID, $CustomerID, $RouteID);
        $stmt->execute();
        $route = $stmt->get_result();
        $stmt->close();
        if($route->num_rows < 1){
                    $stmt = $this->conn->prepare("INSERT INTO notifications(ProductID, CarID, SenderID, ReceiverID, RouteID) values ( ?, ?, ?, ?, ? )");
                    $stmt->bind_param("sssss", $ProductID,$CarID,$CarrierID,$CustomerID,$RouteID);
                    $stmt->execute();
                    $stmt->close();
                    $new_notification_id = $this->conn->insert_id;
                    
                $stmt = $this->conn->prepare("SELECT UserFcmToken FROM user WHERE UserMail = (SELECT CustomerMail FROM customer WHERE CustomerID = ?)");
                $stmt->bind_param("s",$CustomerID);
                $stmt->execute();
                $FcmToken = $stmt->get_result();
                $stmt->close();
                
                $UserFcmToken = array();
                while($fcm = $FcmToken->fetch_assoc()){
                    $UserFcmToken["UserFcmToken"] = $fcm["UserFcmToken"];
                }
                
                
                $stmt = $this->conn->prepare("SELECT UserName FROM user WHERE UserMail = ( SELECT CarrierMail FROM carrier WHERE CarrierID = ? )");
                $stmt->bind_param("s",$CarrierID);
                $stmt->execute();
                $UserName = $stmt->get_result();
                $stmt->close();
                
                
                $UserNamee = array();
                while($fcm = $UserName->fetch_assoc()){
                    $UserNamee["UserName"] = $fcm["UserName"];
                }
                        
                $stmt = $this->conn->prepare("SELECT ProductName FROM product WHERE ProductID = ?");
                $stmt->bind_param("s",$ProductID);
                $stmt->execute();
                $ProductName = $stmt->get_result();
                $stmt->close();     
                
                
                $ProductNamee = array();
                while($fcm = $ProductName->fetch_assoc()){
                    $ProductNamee["ProductName"] = $fcm["ProductName"];
                }
                $Message = $UserNamee["UserName"]." adli kullanici ".$ProductNamee["ProductName"]." urununu taşımak istiyor";
                $this->sendNotification(str_replace('"', '', $UserFcmToken["UserFcmToken"]), $Message);
            if ($route) {

               return $new_notification_id;
            } else {
               return NULL;
            }
        }else{
            return false;
        }  
    }
    
    /* FCM Notification Send */
    

    
    public function sendNotification($FcmToken, $Message){
  
        
        $url = 'https://fcm.googleapis.com/fcm/send';

        $msg = array
        (
            'message' 	=> $Message,
            'title'		=> "Ecargo Notification",
            //'subtitle'	=> 'This is a subtitle. subtitle',
            //'tickerText'	=> 'Ticker text here...Ticker text here...Ticker text here',
            //'vibrate'	=> 1,
            //'sound'		=> 1,
            //'largeIcon'	=> 'large_icon',
            //'smallIcon'	=> 'small_icon'
        );

        $fields = array (
            'to' => $FcmToken,
            'notification' => array('title'=>"Ecargo Notification", 'body'=>$msg)
        );
        $fields = json_encode ( $fields ); 

        $headers = array (
            'Authorization: key='."AAAAizRxx14:APA91bFLZ7u9Fr95HhirVZW5x8jJOwLnDTBLBIHg1p9yu2Si89JNHhhX5-RAofr1HeDihKCL7_3Q_qpRpsU1Z4lUWsqlfYj5TAyZ1unVETIPj3weBzTUEa9ETz6nfz2TL3r1fZ-JRVoq",
            'Content-Type: application/json'
        );
        $ch = curl_init ();
        curl_setopt ( $ch, CURLOPT_URL, $url );
        curl_setopt ( $ch, CURLOPT_POST, true );
        curl_setopt ( $ch, CURLOPT_HTTPHEADER, $headers );
        curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, true );
        curl_setopt ( $ch, CURLOPT_SSL_VERIFYPEER, true );
        curl_setopt ( $ch, CURLOPT_POSTFIELDS, $fields );
        $result = curl_exec ( $ch );

        curl_close($ch);
        return $result;
 
    }
    
    /* ------------- */
    
    public function listAllCarrierNotifications($CarrierID){
        $stmt = $this->conn->prepare("SELECT * FROM notifications WHERE ReceiverID = ?");
        $stmt->bind_param("i",$CarrierID);
        $stmt->execute();
        
        $notifications = $stmt->get_result();
        $stmt->close();
        
        return $notifications;
        
    }
    
        public function listAllCustomerNotifications($CustomerID){
        $stmt = $this->conn->prepare("SELECT * FROM notifications WHERE ReceiverID = ?");
        $stmt->bind_param("i",$CustomerID);
        $stmt->execute();
        
        $notifications = $stmt->get_result();
        $stmt->close();
        
        return $notifications;
        
    }
    
    public function deleteNotification($NotificationID){
        $stmt = $this->conn->prepare("DELETE FROM notifications WHERE NotificationID = ?");
        $stmt->bind_param("s",$NotificationID);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
        
    }
    //------//
    
    //--//
    // Relation Methods
    
    public function createRelation($RouteID, $ProductID, $SenderID, $UserType){
        $stmt = $this->conn->prepare("SELECT * FROM service WHERE RouteID = ? AND ProductID = ?");
        $stmt->bind_param("ii", $RouteID, $ProductID);
        $stmt->execute();
        $route = $stmt->get_result();
        $stmt->close();
        if($route->num_rows < 1){
                    $stmt = $this->conn->prepare("INSERT INTO service(RouteID, ProductID) values ( ?, ? )");
                    $stmt->bind_param("ii", $RouteID,$ProductID);
                    $stmt->execute();
                    $stmt->close();
                    $new_notification_id = $this->conn->insert_id;
                    
                    if($UserType === "Customer"){
                        $stmt = $this->conn->prepare("SELECT UserFcmToken FROM user WHERE UserMail = ( SELECT  CustomerMail FROM customer WHERE CustomerID = ?)");
                        $stmt->bind_param("i",$SenderID);
                        $stmt->execute();
                        $FcmToken = $stmt->get_result();
                        $stmt->close();
                        
                            $FcmTokenn = array();
                            while($fcm = $FcmToken->fetch_assoc()){
                                $FcmTokenn["UserFcmToken"] = $fcm["UserFcmToken"];
                            }
                        
                        if($FcmToken->num_rows > 0){
                            
                            $stmt = $this->conn->prepare("SELECT UserName FROM user WHERE UserMail = (SELECT CarrierMail FROM carrier WHERE CarrierID = (SELECT CarrierID FROM route WHERE RouteID = ?))");
                            $stmt->bind_param("s",$RouteID);
                            $stmt->execute();
                            $UserName = $stmt->get_result();
                            $stmt->close();
                            
                            $UserNamee = array();
                            while($fcm = $UserName->fetch_assoc()){
                                $UserNamee["UserName"] = $fcm["UserName"];
                            }
                            
                            
                            $stmt = $this->conn->prepare("SELECT ProductName FROM product WHERE ProductID = ?");
                            $stmt->bind_param("s",$ProductID);
                            $stmt->execute();
                            $ProductName = $stmt->get_result();
                            $stmt->close();
                            
                            $ProductNamee = array();
                            while($fcm = $ProductName->fetch_assoc()){
                                $ProductNamee["ProductName"] = $fcm["ProductName"];
                            }
                            
                            $Message = $UserNamee["UserName"]." adlı kullanıcı ".$ProductNamee["ProductName"]." ürününüzü taşımayı kabul etti";
                            $this->sendNotification($FcmTokenn["UserFcmToken"], $Message);
                        }
                        
                    }else{
                        $stmt = $this->conn->prepare("SELECT UserFcmToken FROM user WHERE UserMail = ( SELECT  CarrierMail FROM carrier WHERE CarrierID = ?)");
                        $stmt->bind_param("i",$SenderID);
                        $stmt->execute();
                        $FcmToken = $stmt->get_result();
                        $stmt->close();
                        
                            $FcmTokenn = array();
                            while($fcm = $FcmToken->fetch_assoc()){
                                $FcmTokenn["UserFcmToken"] = $fcm["UserFcmToken"];
                            }
                        
                        if($FcmToken->num_rows > 0){
                            
                            $stmt = $this->conn->prepare("SELECT UserName FROM user WHERE UserMail = (SELECT CustomerMail FROM customer WHERE CustomerID =(SELECT CustomerID FROM product WHERE ProductID = ?))");
                            $stmt->bind_param("s",$ProductID);
                            $stmt->execute();
                            $UserName = $stmt->get_result();
                            $stmt->close();
                            
                            $UserNamee = array();
                            while($fcm = $UserName->fetch_assoc()){
                                $UserNamee["UserName"] = $fcm["UserName"];
                            }
                            
                            $stmt = $this->conn->prepare("SELECT ProductName FROM product WHERE ProductID = ?");
                            $stmt->bind_param("s",$ProductID);
                            $stmt->execute();
                            $ProductName = $stmt->get_result();
                            $stmt->close();
                            
                            $ProductNamee = array();
                            while($fcm = $ProductName->fetch_assoc()){
                                $ProductNamee["ProductName"] = $fcm["ProductName"];
                            }
                            
                            $Message = $UserNamee["UserName"]." adlı kullanıcı ".$ProductNamee["ProductName"]." ürününü size taşıtmayı kabul etti";
                            $this->sendNotification($FcmTokenn["UserFcmToken"], $Message);
                        }
                    }
                    
                    
            if ($route) {
               return $new_notification_id;
            } else {
               return NULL;
            }
        }else{
            return FALSE;
        }
    }
    
    public function deleteRelation($CustomerID,$ProductID,$ProductName,$RouteID){
        $stmt = $this->conn->prepare("select user.UserFcmToken from user where user.UserMail = (select CustomerMail from customer where customer.CustomerID = ?)");
        $stmt->bind_param("s",$CustomerID);
        $stmt->execute();
        $UserFcmToken = $stmt->get_result();
        $stmt->close();
        
            $FcmTokenn = array();
            while($fcm = $UserFcmToken->fetch_assoc()){
                $FcmTokenn["UserFcmToken"] = $fcm["UserFcmToken"];
            }
            $Message = $ProductName." ürününüz teslim edilmiştir.";
            $this->sendNotification($FcmTokenn["UserFcmToken"], $Message);
            
            
        $stmt = $this->conn->prepare("DELETE FROM service WHERE service.ProductID= ? ");   
        $stmt->bind_param("s",$ProductID);
        $stmt->execute();
        $stmt->close();
        $stmt = $this->conn->prepare("DELETE FROM product WHERE product.ProductID = ?");   
        $stmt->bind_param("s",$ProductID);
            if($stmt->execute()){
                $stmt->close();
                
                $stmt = $this->conn->prepare("SELECT * FROM service WHERE RouteID = ?");
                $stmt->bind_param("s",$RouteID);
                $stmt->execute();
                $route = $stmt->get_result();
                $stmt->close();
                    if($route->num_rows < 1){
                        $stmt = $this->conn->prepare("DELETE FROM route WHERE RouteID = ?");
                        $stmt->bind_param("s",$RouteID);
                        if($stmt->execute()){
                            $stmt->close();
                            return false;
                        }else{
                        }
                    }else{
                        return true;
                    }
            }else{
                $stmt->close();
                return NULL;
            }   

    }
    
    
    
    
    //--//
    
    
    // Product on Route and All Product Methods
    
    public function allProductsOfCustomer($CustomerID){
        $stmt = $this->conn->prepare("SELECT * FROM product WHERE CustomerID = ? AND NOT EXISTS ( SELECT * FROM service WHERE service.ProductID = product.ProductID)");
        $stmt->bind_param("i",$CustomerID);
        $stmt->execute();
        $Products = $stmt->get_result();
        $stmt->close();
        if($Products->num_rows < 1){
            return NULL;
        }else{
            return $Products;
        }
    }
    
    public function allProductsOnRouteOfCustomer($CustomerID){
        $stmt = $this->conn->prepare("SELECT product.ProductID,CustomerID,ProductName,ProductFrom,ProductTo,ProductDetails,service.RouteID FROM `product` INNER JOIN service ON service.ProductID = product.ProductID WHERE product.CustomerID = ?");
        $stmt->bind_param("i",$CustomerID);
        $stmt->execute();
        $Products = $stmt->get_result();
        $stmt->close();
        return $Products;
    }
    

    
    public function getUserInfoByRouteID($RouteID){
        $stmt = $this->conn->prepare("SELECT UserName,UserSurname,UserMail,UserPhoneNumber FROM user WHERE UserMail = (SELECT CarrierMail FROM carrier WHERE CarrierID = (SELECT CarrierID FROM car WHERE CarID = (SELECT CarID FROM route WHERE RouteID = ?)))");
        $stmt->bind_param("i",$RouteID);
        $stmt->execute();
        $UserInfo = $stmt->get_result();
        $stmt->close();
        return $UserInfo;
    }
    
    // ***** ///
    
        // -- ---- -- //
    // Location Methods //
    
    public function saveLocation($Lat, $Long,$CarrierID){
        
        $stmt = $this->conn->prepare("SELECT * FROM location WHERE CarrierID = ?");
        $stmt->bind_param("s", $CarrierID);
        $stmt->execute();
        $Locations = $stmt->get_result();
        $stmt->close();
        if($Locations->num_rows < 1){
            $stmt = $this->conn->prepare("INSERT INTO location(CarrierLat, CarrierLong, CarrierID) values(?, ?, ?)");
            $stmt->bind_param("sss",$Lat,$Long,$CarrierID);
            $stmt->execute();
        
            $new_notification_id = $this->conn->insert_id;
            return $new_notification_id;
        }elseif($Locations->num_rows == 1){
            $stmt = $this->conn->prepare("UPDATE location SET CarrierLat = ?, CarrierLong = ? WHERE CarrierID = ?");
            $stmt->bind_param("sss",$Lat,$Long,$CarrierID);
            $stmt->execute();
            $stmt->close();
            return true;
        }else{
            return false;
        }
        

        
    }
    
    public function getLocation($ProductID){
        $stmt = $this->conn->prepare("SELECT CarrierLat, CarrierLong FROM location WHERE CarrierID =(SELECT CarrierID FROM car WHERE CarID = (Select CarId FROM route WHERE RouteID = (SELECT RouteID FROM service WHERE ProductID = ?)))");
        $stmt->bind_param("s",$ProductID);
        $stmt->execute();
        $location = $stmt->get_result();
        $stmt->close();
        if($location->num_rows < 1){
            return false;
        }else{
            return $location;
        }
        
        
    }


    // ------ //
    

    /* ------------- `user_tasks` table method ------------------ */

    public function sendVerificationMail($UserMail, $code) {
        $UserID = $this->getIDByMail($UserMail);
        $res = array();
        if (!is_null($UserID)) {
            //$id = $UserID;
            //$key = base64_encode($id);
            $result = $this->getMailToken($UserMail);
            $response = $result['MailToken'];   
            $code = $response;

            $message = "					
						Hello $UserMail,
						<br /><br />
						Welcome to Ecargo!<br/>
						To complete your registration  please , just click following link<br/>
						<br /><br />
						<a href='https://ecargo.info/Api/v1/verification?id=$UserID&code=$code'>Click HERE to Activate</a>
						<br /><br />
						Thanks,";

            $subject = "Confirm Registration";

            $this->send_mail($UserMail, $message, $subject);
            $msg = "
					<div class='alert alert-success'>
						<button class='close' data-dismiss='alert'>&times;</button>
						<strong>Success!</strong>  We've sent an email to $UserMail.
                    Please click on the confirmation link in the email to create your account. 
			  		</div>
					";
            $res["error"] = false;
            $res["Message"] = $msg;
            return $res;
        } else {

            $msg = "sorry , Query could no execute...";
            $res["error"] = true;
            $res["Message"] = $msg;
            return $res;
        }
    }
    
    
    public function VerifyEmail($UserID,$MailToken){
        //$id = base64_decode($UserID);
        $id = $UserID;
	$code = $MailToken;
	
	$statusY = 1;
	$statusN = 0;
	
	$stmt = $this->conn->prepare("SELECT UserID,isEmailVerified FROM user WHERE UserID = ? AND EmailToken = ? LIMIT 1");
	$stmt->bind_param("ss",$id,$code);
        $stmt->execute();

        $location = $stmt->get_result();
        $stmt->close();
        if($location->num_rows > 0)
            {
                $isEmailVerified = array();
                while($fcm = $location->fetch_assoc()){
                    $isEmailVerified["isEmailVerified"] = $fcm["isEmailVerified"];
                    
                }
		if($isEmailVerified['isEmailVerified']==$statusN)
		{
                        
			$stmt = $this->conn->prepare("UPDATE user SET isEmailVerified = ? WHERE UserID =? ");
			$stmt->bind_param("ii",$statusY,$id);
			$stmt->execute();	
			
			$msg = "
		           <div>
					  Your Account is Now Activated : <a href='https://ecargo.info/index.html'>Login here</a>
			       </div>
			       ";
                        $res["error"] = false;
                        $res["Message"] = $msg;
                        return $res;
		}
		else
		{
			$msg = "
		           <div>
					  <strong>Sorry !</strong>  Your Account is already Activated : <a href='https://ecargo.info/index.html'>Login here</a>
			       </div>
			       ";
                        $res["error"] = true;
                        $res["Message"] = $msg;
                        return $res;
		}
	}
	else
	{
		$msg = "
		       <>
			   <strong>Sorry !</strong>  No Account Found : <a href='http://ecargo.info/e-cargo/test_LoginRegister/signup.php'>Signup here</a>
			   </div>
			   ";
                        $res["error"] = true;
                        $res["Message"] = $msg;
                        return $res;

	}	

    }
    
        


    function send_mail($email, $message, $subject) {
        require_once(dirname(__FILE__) . '/mailer/class.phpmailer.php');
        $mail = new PHPMailer();
        $mail->IsSMTP();
        $mail->SMTPDebug = 0;
        $mail->SMTPAuth = true;
        $mail->SMTPSecure = "ssl";
        $mail->Host = "smtp.gmail.com";
        $mail->Port = 465;
        $mail->AddAddress($email);
        $mail->Username = "ecargo.mailer@gmail.com";
        $mail->Password = "ecargomailer";
        $mail->SetFrom('ecargo.mailer@gmail.com', 'E-Cargo');
        $mail->AddReplyTo("ecargo.mailer@gmail.com", "E-Cargo");
        $mail->Subject = $subject;
        $mail->MsgHTML($message);
        $mail->Send();
    }

}

?>
