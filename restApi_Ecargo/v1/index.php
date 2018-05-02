<?php
require_once '../include/DbHandler.php';
require_once '../include/PassHash.php';
require '.././libs/Slim/Slim.php';

\Slim\Slim::registerAutoloader();

$app = new \Slim\Slim();

use lib\Slim\Middleware\SessionCookie;
session_start();

// User id from db - Global Variable
$user_id = NULL;

/**
 * Adding Middle Layer to authenticate every request
 * Checking if the request has valid api key in the 'Authorization' header
 */
function authenticate(\Slim\Route $route) {
    // Getting request headers
    $headers = apache_request_headers();
    $response = array();
    $app = \Slim\Slim::getInstance();
    // Verifying Authorization Header
    if (isset($headers['authorization'])) {
        $db = new DbHandler();

        // get the api key
        $api_key = $headers['authorization'];
        // validating api key
        if (!$db->isValidApiKey($api_key)) {
            // api key is not present in users table
            $response["error"] = true;
            $response["message"] = "Access Denied. Invalid Api key";
            echoRespnse(401, $response);
            $app->stop();
        } else {
            global $user_id;
            // get user primary key id
            $user_id = $db->getUserId($api_key);
        }
    } else {
        // api key is missing in header
        $response["error"] = true;
        $response["message"] = "Api key is misssing";
        echoRespnse(400, $response);
        $app->stop();
    }
}

/**
 * ----------- METHODS WITHOUT AUTHENTICATION ---------------------------------
 */
/**
 * User Registration
 * url - /register
 * method - POST
 * params - name, email, password
 */
$app->post('/register', function() use ($app) {
    // check for required params
    verifyRequiredParams(array(
        'UserName',
        'UserSurname',
        'UserMail',
        'UserPassword',
        'UserSex',
        'UserTcNo',
        'UserPhoneNumber',
        'UserType',
        'FcmToken')
    );

    $response = array();

    // reading post params
    $UserName = $app->request->post('UserName');
    $UserSurname = $app->request->post('UserSurname');
    $UserMail = $app->request->post('UserMail');
    $UserPassword = $app->request->post('UserPassword');
    $UserSex = $app->request->post('UserSex');
    $UserTcNo = $app->request->post('UserTcNo');
    $UserPhoneNumber = $app->request->post('UserPhoneNumber');
    $UserType = $app->request->post('UserType');
    $FcmToken = $app->request->post('FcmToken');

    // validating email address
    validateEmail($UserMail);

    $db = new DbHandler();
    $res = $db->createUser($UserName, $UserSurname, $UserMail, $UserPassword, $UserSex, $UserTcNo, $UserPhoneNumber, $UserType, $FcmToken);

    if ($res == USER_CREATED_SUCCESSFULLY) {
        $response["error"] = false;
        $response["message"] = "You are successfully registered! Please activate your account.";
    } else if ($res == USER_CREATE_FAILED) {
        $response["error"] = true;
        $response["message"] = "Oops! An error occurred while registereing";
    } else if ($res == USER_ALREADY_EXISTED) {
        $response["error"] = true;
        $response["message"] = "Sorry, this email already existed";
    }
    // echo json response
    echoRespnse(201, $response);
});

/**
 * User Login
 * url - /login
 * method - POST
 * params - email, password
 */


$app->post('/login', function() use ($app) {
    // check for required params
    verifyRequiredParams(array('UserMail', 'UserPassword'));
    // reading post params
    $UserEmail = $app->request()->post('UserMail');
    $UserPassword = $app->request()->post('UserPassword');
    $response = array();

    $db = new DbHandler();
    // check for correct email and password
    

    if ($db->checkLogin($UserEmail, $UserPassword)) {
        // get the user by email
        $user = $db->getUserByEmail($UserEmail);

        if ($user != NULL) {
            $response["error"] = false;
            $response['UserName'] = $user['UserName'];
            $response['UserMail'] = $user['UserMail'];
            $response['apiKey'] = $user['api_key'];
            $response['UserSurname'] = $user['UserSurname'];
            $response['UserType'] = $user['UserType'];
            $userType = $user['UserType'];
            if($userType === "Customer"){
                $response['CustomerID'] = $user['CustomerID'];
            }else{
                $response['CarrierID'] = $user['CarrierID'];
            }
           
            
            
            
        } else {
            // unknown error occurred
            $response['error'] = true;
            $response['message'] = "An error occurred. Please try again";
        }
    } else {
        // user credentials are wrong
        $response['error'] = true;
        $response['message'] = 'Incorrect credentials or account not activated';
    }

    echoRespnse(200, $response);
});

$app->post('/getTypeIDCarr', function() use ($app) {
    // check for required params
    verifyRequiredParams(array('UserMail'));
    // reading post params
    $UserEmail = $app->request()->post('UserMail');
    $response = array();

    $db = new DbHandler();
        $user = $db->getCarrierID($UserEmail);

        if ($user != NULL) {
            $response["error"] = false;
            $response['CarrierID'] = $user['CarrierID'];   
        } else {
            // unknown error occurred
            $response['error'] = true;
            $response['message'] = "An error occurred. Please try again";
        }


    echoRespnse(200, $response);
});

$app->post('/getTypeIDCust', function() use ($app) {
    // check for required params
    verifyRequiredParams(array('UserMail'));
    // reading post params
    $UserEmail = $app->request()->post('UserMail');
    $response = array();

        $db = new DbHandler();
        $user = $db->getCustomerID($UserEmail);

        if ($user != NULL) {
            $response["error"] = false;
            $response['CustomerID'] = $user['CustomerID'];   
        } else {
            // unknown error occurred
            $response['error'] = true;
            $response['message'] = "An error occurred. Please try again";
        }
    

    echoRespnse(200, $response);
});

/*
 * ------------------------ METHODS WITH AUTHENTICATION ------------------------
 */

/**
 * Listing all tasks of particual user
 * method GET
 * url /tasks          
 */
$app->post('/products/ListProducts', 'authenticate', function() use($app) {
    verifyRequiredParams(array('CustomerID'));
    $response = array();
    $db = new DbHandler();
    $CustomerID = $app->request()->post('CustomerID');
    // fetching all user tasks
    $result = $db->getAllUserProducts($CustomerID);

    $response["error"] = false;
    $response["products"] = array();

    // looping through result and preparing tasks array
    while ($products = $result->fetch_assoc()) {
        $tmp = array();
        $tmp["ProductID"] = $products["ProductID"];
        $tmp["CustomerID"] = $products["CustomerID"];
        $tmp["ProductName"] = $products["ProductName"];
        $tmp["ProductFrom"] = $products["ProductFrom"];
        $tmp["ProductTo"] = $products["ProductTo"];
        $tmp["ProductDetails"] = $products["ProductDetails"];
        array_push($response["products"], $tmp);
    }

    echoRespnse(200, $response);
});



$app->get('/products/ListAllProducts', 'authenticate', function() {
    $response = array();
    $db = new DbHandler();

    // fetching all user tasks
    $result = $db->getAllProducts();

    $response["error"] = false;
    $response["products"] = array();

    // looping through result and preparing tasks array
    while ($products = $result->fetch_assoc()) {
        $tmp = array();
        $tmp["ProductID"] = $products["ProductID"];
        $tmp["CustomerID"] = $products["CustomerID"];
        $tmp["ProductName"] = $products["ProductName"];
        $tmp["ProductFrom"] = $products["ProductFrom"];
        $tmp["ProductTo"] = $products["ProductTo"];
        $tmp["ProductDetails"] = $products["ProductDetails"];
        array_push($response["products"], $tmp);
    }

    echoRespnse(200, $response);
});

/* add product methods */
$app->post('/products/addNewProduct', 'authenticate', function() use($app) {
    verifyRequiredParams(array('CustomerID','ProductName', 'ProductFrom', 'ProductTo', 'ProductDetails'));

    $response = array();
    $db = new DbHandler();
    $CustomerID = $app->request()->post('CustomerID');
    $ProductName = $app->request()->post('ProductName');
    $ProductFrom = $app->request()->post('ProductFrom');
    $ProductTo = $app->request()->post('ProductTo');
    $ProductDetails = $app->request()->post('ProductDetails');
    // creating new task
    $product_id = $db->createProduct($CustomerID, $ProductName, $ProductFrom, $ProductTo, $ProductDetails);

    if ($product_id != NULL) {
        $response["error"] = false;
        $response["message"] = "Product created successfully";
        $response["ProductID"] = $product_id;
        echoRespnse(201, $response);
    }elseif($product_id == false){
        $response["error"] = true;
        $response["message"] = "You already have that product.";
        echoRespnse(200, $response);
    }else {
        $response["error"] = true;
        $response["message"] = "Failed to create product. Please try again";
        echoRespnse(200, $response);
    }
});
/* listing single product of particular user */
$app->post('/products/getProduct', 'authenticate', function() use($app) {
    verifyRequiredParams(array('CustomerID','ProductID'));
    
    $ProductID = $app->request()->post('ProductID');
    $CustomerID = $app->request()->post('CustomerID');
    
    $response = array();
    $db = new DbHandler();

    // fetch task
    $result = $db->getProduct($ProductID, $CustomerID);

    if ($result != NULL) {
        if (is_null($result["ProductID"])) {
            $response["error"] = true;
            $response["message"] = "The requested resource doesn't exists";
            echoRespnse(404, $response);
        } else {
            $response["error"] = false;
            $response["ProductID"] = $result["ProductID"];
            $response["CustomerID"] = $result["CustomerID"];
            $response["ProductName"] = $result["ProductName"];
            $response["ProductFrom"] = $result["ProductFrom"];
            $response["ProductTo"] = $result["ProductTo"];
            $response["ProductDetails"] = $result["ProductDetails"];
            echoRespnse(200, $response);
        }
        
    } else {
        $response["error"] = true;
        $response["message"] = "The requested resource doesn't exists";
        echoRespnse(404, $response);
    }
});


$app->post('/products/getProductByRoute', 'authenticate', function() use($app) {
    verifyRequiredParams(array('RouteID'));
    
    $RouteID = $app->request()->post('RouteID');
    $response = array();
    $db = new DbHandler();

    // fetch task
    $result = $db->getAllProductsByRouteID($RouteID);


    $response["error"] = false;
    $response["products"] = array();
    // looping through result and preparing tasks array
    while ($products = $result->fetch_assoc()) {
        $tmp = array();
        $tmp["ProductID"] = $products["ProductID"];
        $tmp["CustomerID"] = $products["CustomerID"];
        $tmp["ProductName"] = $products["ProductName"];
        $tmp["ProductFrom"] = $products["ProductFrom"];
        $tmp["ProductTo"] = $products["ProductTo"];
        $tmp["ProductDetails"] = $products["ProductDetails"];
        array_push($response["products"], $tmp);
    }
    echoRespnse(200, $response);
});



/**
 * Updating existing task
 * method PUT
 * params task, status
 * url - /tasks/:id
 */
$app->put('/products/update/:id', 'authenticate', function($ProductID) use($app) {
    // check for required params
    verifyRequiredParams(array('ProductName', 'ProductFrom', 'ProductTo', 'ProductDetails'));

    global $user_id;
    $ProductName = $app->request->put('ProductName');
    $ProductFrom = $app->request->put('ProductFrom');
    $ProductTo = $app->request->put('ProductTo');
    $ProductDetails = $app->request->put('ProductDetails');

    $db = new DbHandler();
    $response = array();

    // updating task
    $result = $db->updateProduct($user_id, $ProductName, $ProductFrom, $ProductTo, $ProductID, $ProductDetails);
    if ($result) {
        // task updated successfully
        $response["error"] = false;
        $response["message"] = "Product updated successfully";
    } else {
        // task failed to update
        $response["error"] = true;
        $response["message"] = "Product failed to update. Please try again!";
    }
    echoRespnse(200, $response);
});

/**
 * Deleting task. Users can delete only their tasks
 * method DELETE
 * url /tasks
 */
$app->post('/products/deleteProduct', 'authenticate', function() use($app) {
    verifyRequiredParams(array('ProductID'));

    $db = new DbHandler();
    $response = array();
    $ProductID = $app->request()->post('ProductID');
    $result = $db->deleteProduct($ProductID);
    if ($result) {
        // task deleted successfully
        $response["error"] = false;
        $response["message"] = "Product deleted succesfully";
    }elseif($result == false){
        $response["error"] = true;
        $response["message"] = "Failed to delete.Product is on a active route!";
    } else {
        // task failed to delete
        $response["error"] = true;
        $response["message"] = "Product failed to delete. Please try again!";
    }
    echoRespnse(200, $response);
});

/**
 * Listing all cars of particual user
 * method GET
 * url /tasks          
 */
$app->post('/cars/ListCars', 'authenticate', function() use($app) {
    verifyRequiredParams(array('CarrierID'));
    
    $response = array();
    $db = new DbHandler();
    
    $CarrierID = $app->request()->post('CarrierID');
    // fetching all user tasks
    $result = $db->getAllUserCars($CarrierID);

    $response["error"] = false;
    $response["cars"] = array();

    // looping through result and preparing tasks array
    while ($products = $result->fetch_assoc()) {
        $tmp = array();
        $tmp["CarID"] = $products["CarID"];
        $tmp["CarrierID"] = $products["CarrierID"];
        $tmp["CarPlate"] = $products["CarPlate"];
        $tmp["CarBrand"] = $products["CarBrand"];
        $tmp["CarModel"] = $products["CarModel"];
        array_push($response["cars"], $tmp);
    }

    echoRespnse(200, $response);
});

/* add product methods */
$app->post('/cars/addNewCar', 'authenticate', function() use($app) {
    verifyRequiredParams(array('CarrierID','CarPlate', 'CarBrand', 'CarModel'));

    $response = array();
    $db = new DbHandler();

    $CarrierID = $app->request()->post('CarrierID');
    $CarPlate = $app->request()->post('CarPlate');
    $CarBrand = $app->request()->post('CarBrand');
    $CarModel = $app->request()->post('CarModel');
    // creating new task
    $car_id = $db->createCar($CarrierID, $CarPlate, $CarBrand, $CarModel);

    if ($car_id != NULL) {
        $response["error"] = false;
        $response["message"] = "Car created successfully";
        $response["CarID"] = $car_id;
        echoRespnse(201, $response);
    }elseif($car_id == false){
        $response["error"] = true;
        $response["message"] = "You already have that car.";
        echoRespnse(200, $response);
    } else {
        $response["error"] = true;
        $response["message"] = "Failed to create car. Please try again";
        echoRespnse(200, $response);
    }
});
/* listing single product of particular user */
$app->get('/cars/:id', 'authenticate', function($CarID) {
    global $user_id;
    $response = array();
    $db = new DbHandler();

    // fetch task
    $result = $db->getCar($CarID, $user_id);

    if ($result != NULL) {
        if (is_null($result["CarID"])) {
            $response["error"] = true;
            $response["message"] = "The requested resource doesn't exists";
            echoRespnse(404, $response);
        } else {
            $response["error"] = false;
            $response["CarID"] = $result["CarID"];
            $response["CarrierID"] = $result["CarrierID"];
            $response["CarPlate"] = $result["CarPlate"];
            $response["CarModel"] = $result["CarModel"];
            $response["CarBrand"] = $result["CarBrand"];
            echoRespnse(200, $response);
        }
        
    } else {
        $response["error"] = true;
        $response["message"] = "The requested resource doesn't exists";
        echoRespnse(404, $response);
    }
});

/**
 * Updating existing task
 * method PUT
 * params task, status
 * url - /tasks/:id
 */
$app->put('/cars/update', 'authenticate', function() use($app) {
    // check for required params
    verifyRequiredParams(array('CarID', 'CarPlate', 'CarBrand', 'CarModel'));

    $CarID = $app->request()->put('CarID');
    $CarPlate = $app->request->put('CarPlate');
    $CarBrand = $app->request->put('CarBrand');
    $CarModel = $app->request->put('CarModel');

    $db = new DbHandler();
    $response = array();

    // updating task
    $result = $db->updateCar($CarPlate, $CarBrand, $CarModel, $CarID);
    if ($result) {
        // task updated successfully
        $response["error"] = false;
        $response["message"] = "Car updated successfully";
    } else {
        // task failed to update
        $response["error"] = true;
        $response["message"] = "Car failed to update. Please try again!";
    }
    echoRespnse(200, $response);
});

/**
 * Deleting task. Users can delete only their tasks
 * method DELETE
 * url /tasks
 */
$app->post('/cars/deleteCar', 'authenticate', function() use($app) {
    verifyRequiredParams(array('CarID'));

    $db = new DbHandler();
    $response = array();
    $CarID = $app->request()->post('CarID');
    $result = $db->deleteCar($CarID);
    if ($result) {
        // task deleted successfully
        $response["error"] = false;
        $response["message"] = "Car deleted succesfully";
    }elseif($result == false){
        $response["error"] = true;
        $response["message"] = "Failed to delete. Car is on a active route!";
    } else {
        // task failed to delete
        $response["error"] = true;
        $response["message"] = "Car failed to delete. Please try again!";
    }
    echoRespnse(200, $response);
});

/** Creating Route with CarrierID  **/

$app->post('/carrier/CreateRoute','authenticate',function() use($app) {
    verifyRequiredParams(array('CarID','RouteFrom', 'RouteTo', 'RouteDate'));

    $db = new DbHandler();
    $response = array();
    
    $CarID = $app->request()->post('CarID');
    $RouteFrom = $app->request()->post('RouteFrom');
    $RouteTo = $app->request()->post('RouteTo');
    $RouteDate = $app->request()->post('RouteDate');
   
    $RouteID = $db->createRoute($CarID, $RouteFrom, $RouteTo, $RouteDate);
    
    $FormattedDate = str_replace("-","/",$RouteDate);
    $TodayDate = Date("Y/m/d");
    
    if($FormattedDate < $TodayDate){
        $response["error"] = true;
        $response["message"] = "bugünün tarihinden geriye gidemezsiniz";
        choRespnse(200, $response);
    }else{
         if($RouteID == false){
            $response["error"] = true;
            $fulldate = explode(" ", str_replace('"', "", $RouteDate));
            $date = $fulldate[0];
            $format = explode("-",$date);
            $response["message"] = "You already have that route on ". $format[2]."/".$format[1]."/".$format[0];
            echoRespnse(200, $response);
        }elseif($RouteID == true){
            $response["error"] = false;
            $response["message"] = "Route created successfully";
            $response["RouteID"] = $RouteID;
            echoRespnse(201, $response);   
        }else {
            $response["error"] = true;
            $response["message"] = "Failed to create route. Please try again";
            echoRespnse(200, $response);
        }
    }
    


        
    

    
});
$app->post('/routes/ListRoutesByCarrierID', 'authenticate', function() use($app) {
    verifyRequiredParams(array('CarrierID'));
    $db = new DbHandler();
    
    $CarrierID = $app->request()->post('CarrierID');
    
    $Routes = $db->listRoutesByCarrierID($CarrierID);
    $response["error"] = false;   
    $response["routes"] = array();

    // looping through result and preparing tasks array
    while ($result = $Routes->fetch_assoc()) {
        $tmp = array();
        $tmp["RouteID"] = $result["RouteID"];
        $tmp["CarID"] = $result["CarID"];
        $tmp["RouteFrom"] = $result["RouteFrom"];
        $tmp["RouteTo"] = $result["RouteTo"];
        $tmp["RouteDate"] = $result["RouteDate"];
        array_push($response["routes"], $tmp);
    }
    echoRespnse(200, $response);
});

$app->post('/routes/ListRoutesByCarPlate', 'authenticate', function() use($app) {
    verifyRequiredParams(array('CarPlate'));
    $db = new DbHandler();
    
    $CarPlate = $app->request()->post('CarPlate');
    
    $Routes = $db->listRoutesByCarID($CarPlate);
    $response["error"] = false;   
    $response["routes"] = array();

    // looping through result and preparing tasks array
    while ($result = $Routes->fetch_assoc()) {
        $tmp = array();
        $tmp["RouteID"] = $result["RouteID"];
        $tmp["CarID"] = $result["CarID"];
        $tmp["RouteFrom"] = $result["RouteFrom"];
        $tmp["RouteTo"] = $result["RouteTo"];
        $tmp["RouteDate"] = $result["RouteDate"];
        array_push($response["routes"], $tmp);
    }
    echoRespnse(200, $response);
});
$app->post('/routes/ListRoutes', 'authenticate', function() use($app) {
   
    $response = array();
    $db = new DbHandler();
    
    // fetching all user tasks
    $result = $db->listAllRoutes();

    $response["error"] = false;
    
    $response["routes"] = array();
    $response["userInfo"] = array();
    
    
    
    // looping through result and preparing tasks array
    while ($products = $result->fetch_assoc()) {
        $tmp = array();
        $tmp2 = array();
        
        
        $tmp["RouteID"] = $products["RouteID"];
        $tmp["CarID"] = $products["CarID"];
        $tmp["RouteFrom"] = $products["RouteFrom"];
        $tmp["RouteTo"] = $products["RouteTo"];
        $tmp["RouteDate"] = $products["RouteDate"];
        
        $CarID = $products["CarID"];
        
        $result3 = $db->getCarrierIdByCarID($CarID);
        
        while($carrierID = $result3->fetch_assoc()){
            $tmp["CarrierID"] = $carrierID["CarrierID"];
        }
        
        $result2 = $db->getUserInfoByCarID($CarID);
        
        while($user = $result2->fetch_assoc()){
            
            $tmp2["UserName"] = $user["UserName"];
            $tmp2["UserSurname"] = $user["UserSurname"];
            $tmp2["UserPhoneNumber"] = $user["UserPhoneNumber"];
            $tmp2["UserMail"] = $user["UserMail"];
        }
        $tmp["UserInfo"] = $tmp2;
        
        
    
        
        array_push($response["routes"], $tmp);
    }

    echoRespnse(200, $response);
});

$app->post('/routes/deleteRoute', 'authenticate', function() use($app) {
    verifyRequiredParams(array('RouteID'));

    $db = new DbHandler();
    $response = array();
    $RouteID = $app->request()->post('RouteID');
    $result = $db->deleteRoute($RouteID);
    if ($result) {
        // task deleted successfully
        $response["error"] = false;
        $response["message"] = "Route deleted succesfully";
    }elseif($result == false){
        $response["error"] = true;
        $response["message"] = "Failed to delete. Route is on service!";
    } else {
        // task failed to delete
        $response["error"] = true;
        $response["message"] = "Route failed to delete. Please try again!";
    }
    echoRespnse(200, $response);
});

$app->post('/cars/ListCarsByID', 'authenticate', function() use($app) {
    verifyRequiredParams(array('CarID'));
    
    $response = array();
    $db = new DbHandler();
    
    $CarID = $app->request()->post('CarID');
    // fetching all user tasks
    $result = $db->listRoutesByID($CarID);

    $response["error"] = false;
    $response["routes"] = array();

    // looping through result and preparing tasks array
    while ($products = $result->fetch_assoc()) {
        $tmp = array();
        $tmp["CarID"] = $products["CarID"];
        $tmp["CarrierID"] = $products["CarrierID"];
        $tmp["CarPlate"] = $products["CarPlate"];
        $tmp["CarBrand"] = $products["CarBrand"];
        $tmp["CarModel"] = $products["CarModel"];
        array_push($response["cars"], $tmp);
    }
        
    

    echoRespnse(200, $response);
});

//Notification Methods

$app->post('/notifications/AddNotificationCustomer','authenticate', function() use($app){

    verifyRequiredParams(array('ProductID', 'CarID', 'CustomerID', 'CarrierID', 'RouteID'));
    $response = array();
    $db = new DbHandler();
    
    $ProductID = $app->request()->post('ProductID');
    $CarID = $app->request()->post('CarID');
    $CustomerID = $app->request()->post('CustomerID');
    $CarrierID = $app->request()->post('CarrierID');
    $RouteID = $app->request()->post('RouteID');
    
    $result = $db->addNotificationCustomer($ProductID, $CarID, $CustomerID, $CarrierID, $RouteID);
    
    if ($result != NULL) {
        $response["error"] = false;
        $response["message"] = "Notification created successfully";
        $response["NotificationID"] = $result;
        echoRespnse(201, $response);
    } elseif($result == false) {
        $response["error"] = true;
        $response["message"] = "This request is already made.";
        echoRespnse(200, $response);
    }else{
        $response["error"] = true;
        $response["message"] = "Request failed! Try again.";
        echoRespnse(200, $response);
    }
    
    
});

$app->post('/notifications/AddNotificationCarrier','authenticate', function() use($app){

    verifyRequiredParams(array('ProductID', 'CustomerID', 'CarrierID', 'RouteTo', 'RouteFrom'));
    $response = array();
    $tmp = array();
    $db = new DbHandler();
    
    $RouteFrom = $app->request()->post('RouteFrom');
    $RouteTo = $app->request()->post('RouteTo');
    
    $RouteInformation = $db->searchRoutesByLocation($RouteFrom, $RouteTo);
    
    if($RouteInformation == FALSE){
        $response["error"] = true;
        $response["message"] = "There is no route to match.";
        echoRespnse(200, $response);
    }else{
        while($routes = $RouteInformation->fetch_assoc()){    
            $CarID = $routes["CarID"];
            $RouteID = $routes["RouteID"];
        }
        $ProductID = $app->request()->post('ProductID');
        $CustomerID = $app->request()->post('CustomerID');
        $CarrierID = $app->request()->post('CarrierID');
        $result = $db->addNotificationCarrier($ProductID, $CarID, $CarrierID, $CustomerID, $RouteID);
    
    if ($result != NULL) {
        $response["error"] = false;
        $response["message"] = "Notification created successfully";
        $response["NotificationID"] = $result;
        echoRespnse(201, $response);
    } elseif($result == false) {
        $response["error"] = true;
        $response["message"] = "This request is already made.";
        echoRespnse(200, $response);
    }else{
        $response["error"] = true;
        $response["message"] = "Request failed! Try again.";
        echoRespnse(200, $response);
    }
    }
    
    
    
    
});

$app->post('/notifications/ListCarrierNotifications', 'authenticate', function() use($app) {
    verifyRequiredParams(array('ApiKey'));
    
    $response = array();
    $db = new DbHandler();
    
    $ApiKey = $app->request()->post('ApiKey');
    
    $CarrierID = $CarrierID = $db->getCarrierIdByApiKey($ApiKey);
    // fetching all user tasks
    $result = $db->listAllCarrierNotifications($CarrierID);

    $response["error"] = false;
    $response["Notifications"] = array();

    // looping through result and preparing tasks array
    while ($products = $result->fetch_assoc()) {
        $tmp = array();
        
        $tmp["NotificationID"] = $products["NotificationID"];
        $tmp["ProductID"] = $products["ProductID"];
        $tmp["CarID"] = $products["CarID"];
        $tmp["CustomerID"] = $products["SenderID"];
        $tmp["CarrierID"] = $products["ReceiverID"];
        $tmp["RouteID"] = $products["RouteID"];
        $tmp["CustomerSenderID"] = $products["SenderID"];
        
        $CustID = $products["SenderID"];
        $CarrID = $products["ReceiverID"];
        $ProID = $products["ProductID"];
                
        $result2 = $db->getProduct($ProID,$CustID);
        
        while($productinfo = $result2->fetch_assoc()){
            $tmp["ProductID"] = $productinfo["ProductID"];
            $tmp["ProductName"] = $productinfo["ProductName"];
            $tmp["ProductFrom"] = $productinfo["ProductFrom"];
            $tmp["ProductTo"] = $productinfo["ProductTo"];
            $tmp["ProductDetails"] = $productinfo["ProductDetails"];    
        }
      
       // $tmp["ProductInformation"] = $tmp2;
        
       $result3 = $db->getCustomerInfoByID($CustID);
       
       while($customerinfo = $result3->fetch_assoc()){
            $tmp["UserName"] = $customerinfo["UserName"];
            $tmp["UserSurname"] = $customerinfo["UserSurname"];
            $tmp["UserMail"] = $customerinfo["UserMail"];
            $tmp["UserPhoneNumber"] = $customerinfo["UserPhoneNumber"]; 
        }
      
       // $tmp["CustomerInformation"] = $tmp3;
        
        array_push($response["Notifications"], $tmp);
    }

    echoRespnse(200, $response);
});

$app->post('/notifications/ListCustomerNotifications', 'authenticate', function() use($app) {
    verifyRequiredParams(array('ApiKey'));
    
    $response = array();
    $db = new DbHandler();
    
    $ApiKey = $app->request()->post('ApiKey');
    
    $CustomerID = $db->getCustomerIdByApiKey($ApiKey);
    // fetching all user tasks
    $result = $db->listAllCustomerNotifications($CustomerID);

    $response["error"] = false;
    $response["Notifications"] = array();
    
        while ($products = $result->fetch_assoc()) {
        $tmp = array();
        
        $tmp["NotificationID"] = $products["NotificationID"];
        $tmp["ProductID"] = $products["ProductID"];
        $tmp["CarID"] = $products["CarID"];
        $tmp["CustomerID"] = $products["ReceiverID"];
        $tmp["CarrierID"] = $products["SenderID"];
        $tmp["RouteID"] = $products["RouteID"];
        $tmp["CarrierSenderID"] = $products["SenderID"];
        
        $CustID = $products["ReceiverID"];
        $CarrID = $products["SenderID"];
        $ProID = $products["RouteID"];
        $ProductId = $products["ProductID"];
                
        $result2 = $db->getRoute($ProID);
        
        while($productinfo = $result2->fetch_assoc()){
            $tmp["RouteID"] = $productinfo["RouteID"];
            $tmp["CarID"] = $productinfo["CarID"];
            $tmp["RouteFrom"] = $productinfo["RouteFrom"];
            $tmp["RouteTo"] = $productinfo["RouteTo"];
            $tmp["RouteDate"] = $productinfo["RouteDate"];    
        }
        
        $result4 = $db->getProduct($ProductId,$CustID);
        
        while($productinfo1 = $result4->fetch_assoc()){
            $tmp["ProductName"] = $productinfo1["ProductName"]; 
            $tmp["ProductDetails"] = $productinfo1["ProductDetails"];
        }
      
       // $tmp["ProductInformation"] = $tmp2;
        
       $result3 = $db->getCarrierInfoByID($CarrID);
       
       while($customerinfo = $result3->fetch_assoc()){
            $tmp["UserName"] = $customerinfo["UserName"];
            $tmp["UserSurname"] = $customerinfo["UserSurname"];
            $tmp["UserMail"] = $customerinfo["UserMail"];
            $tmp["UserPhoneNumber"] = $customerinfo["UserPhoneNumber"]; 
        }
      
       // $tmp["CustomerInformation"] = $tmp3;
        
        array_push($response["Notifications"], $tmp);
    }

    echoRespnse(200, $response);
});
    

$app->post('/notifications/DeleteNotification','authenticate', function() use($app){
    verifyRequiredParams(array('NotificationID'));

    $db = new DbHandler();
    $response = array();
    $NotificationID = $app->request()->post('NotificationID');
    $result = $db->deleteNotification($NotificationID);
    if ($result > 0) {
        // task deleted successfully
        $response["error"] = false;
        $response["message"] = "Notification deleted succesfully";
    } else {
        // task failed to delete
        $response["error"] = true;
        $response["message"] = "Notification failed to delete. Please try again!";
    }
    echoRespnse(200, $response);
    
});

//------/

// ----- //
// *** Service Methods ** //

$app->post('/services/createRelation', 'authenticate', function() use($app){

    verifyRequiredParams(array('RouteID', 'ProductID', 'SenderID', 'UserType'));
    $db = new DbHandler();
    $response = array();
    
    $RouteID = $app->request()->post("RouteID");
    $ProductID = $app->request()->post("ProductID");
    $SenderID = $app->request()->post("SenderID");
    $UserType = $app->request()->post("UserType");
    
    $result = $db->createRelation($RouteID,$ProductID,$SenderID,$UserType);
    
    if ($result != NULL) {
        $response["error"] = false;
        $response["message"] = "Relation created successfully";
        $response["NotificationID"] = $result;
        echoRespnse(201, $response);
    } elseif($result == false) {
        $response["error"] = true;
        $response["message"] = "This relation is already created !";
        echoRespnse(200, $response);
    }else{
        $response["error"] = true;
        $response["message"] = "Relation creation failed! Try again.";
        echoRespnse(200, $response);
    }
});

$app->post('/services/deleteRelation', 'authenticate', function() use($app){

    verifyRequiredParams(array('RouteID', 'ProductID', 'CustomerID', 'ProductName'));
    $db = new DbHandler();
    $response = array();
    
    $RouteID = $app->request()->post("RouteID");
    $ProductID = $app->request()->post("ProductID");
    $CustomerID = $app->request()->post("CustomerID");
    $ProductName = $app->request()->post("ProductName");
    $result = $db->deleteRelation($CustomerID,$ProductID,$ProductName,$RouteID);
    
    if ($result != NULL) {
        $response["error"] = false;
        $response["message"] = "Failed to delete!";
        echoRespnse(201, $response);
    } elseif($result == false) {
        $response["error"] = true;
        $response["message"] = "Relation deleted.";
        echoRespnse(200, $response);
    }else{
        $response["error"] = true;
        $response["message"] = "There is still products in the route !";
        echoRespnse(200, $response);
    }
});

/* --------------- */ 


// FCM methods

$app->put('/user/UpdateFcm', function() use($app){

    verifyRequiredParams(array('UserMail', 'FcmToken'));
    $db = new DbHandler();
    $response = array();
    
    $UserMail = $app->request()->post("UserMail");
    $FcmToken = $app->request()->post("FcmToken");
    
    $result = $db->updateFcm($UserMail, $FcmToken);
    
    if ($result != NULL) {
        $response["error"] = false;
        $response["message"] = "Fcm token updated successfully";
        $response["NotificationID"] = $result;
        echoRespnse(201, $response);
    } elseif($result == false) {
        $response["error"] = true;
        $response["message"] = "Fcm token is same as previous one.!";
        echoRespnse(200, $response);
    }else{
        $response["error"] = true;
        $response["message"] = "Fcm token update failed! Try again.";
        echoRespnse(200, $response);
    }
});

// ***** //

$app->post('/products/AllProductsOnRoute' ,'authenticate', function() use($app){

    verifyRequiredParams(array('CarrierID'));
    $db = new DbHandler();
    $response = array();
    
    $CarrierID = $app->request()->post("CarrierID");
    // RETUSN PRODUCTS THAT ARE NOT IN ROUTE!
    $result = $db->getAllProductsOnRouteByCarrierID($CarrierID);
    
    if($result == NULL){
         $response["error"] = true;
         $response["message"] = "You do not have any product on route.";
    }else{
        $response["error"] = false;
        $response["AllProductsOnRoute"] = array();

        // looping through result and preparing tasks array
        while ($products = $result->fetch_assoc()) {
            $tmp = array();
            $tmp["ProductID"] = $products["ProductID"];
            $tmp["CustomerID"] = $products["CustomerID"];
            $tmp["ProductName"] = $products["ProductName"];
            $tmp["ProductFrom"] = $products["ProductFrom"];
            $tmp["ProductTo"] = $products["ProductTo"];
            $tmp["ProductDetails"] = $products["ProductDetails"];
            array_push($response["AllProductsOnRoute"], $tmp);
        }
    }

    echoRespnse(200, $response);
    
});

// *** //
// Product on route and all product methods for customer

$app->post('/products/AllProductsAndOnRoute' ,'authenticate', function() use($app){

    verifyRequiredParams(array('CustomerID'));
    $db = new DbHandler();
    $response = array();
    
    $CustomerID = $app->request()->post("CustomerID");
    // RETUSN PRODUCTS THAT ARE NOT IN ROUTE!
    $result = $db->allProductsOfCustomer($CustomerID);
    if($result == NULL){
         $response["error"] = true;
         $response["message"] = "All of your products in route.";
    }else{
        $response["error"] = false;
        $response["AllProducts"] = array();

        // looping through result and preparing tasks array
        while ($products = $result->fetch_assoc()) {
            $tmp = array();
            $tmp["ProductID"] = $products["ProductID"];
            $tmp["CustomerID"] = $products["CustomerID"];
            $tmp["ProductName"] = $products["ProductName"];
            $tmp["ProductFrom"] = $products["ProductFrom"];
            $tmp["ProductTo"] = $products["ProductTo"];
            $tmp["ProductDetails"] = $products["ProductDetails"];
            array_push($response["AllProducts"], $tmp);
        }
    }
        
    $result2 = $db->allProductsOnRouteOfCustomer($CustomerID);
        if($result2 == NULL){
            $response["error"] = true;
            $response["message"] = "You have no products on route";
        }else{
        
    
            $response["error"] = false;
            $response["ProductsOnRoute"] = array();

                // looping through result and preparing tasks array
            $tmp2 = array();
            while ($products2 = $result2->fetch_assoc()) {
            
            $tmp2["ProductID"] = $products2["ProductID"];
            $tmp2["CustomerID"] = $products2["CustomerID"];
            $tmp2["ProductName"] = $products2["ProductName"];
            $tmp2["ProductFrom"] = $products2["ProductFrom"];
            $tmp2["ProductTo"] = $products2["ProductTo"];
            $tmp2["ProductDetails"] = $products2["ProductDetails"];
            $tmp2["RouteID"] = $products2["RouteID"];
            
            $result3 = $db->getUserInfoByRouteID($tmp2["RouteID"]);
            
            while ($products2 = $result3->fetch_assoc()) {
                $tmp2["CarrierName"] = $products2["UserName"];
                $tmp2["CarrierSurname"] = $products2["UserSurname"];
                $tmp2["CarrierMail"] = $products2["UserMail"];
                $tmp2["CarrierPhoneNumber"] = $products2["UserPhoneNumber"];
        }
            
            array_push($response["ProductsOnRoute"], $tmp2);
        }

        
    }
    


    echoRespnse(200, $response);
    
});


$app->post('/products/AllProductsOnRoute' ,'authenticate', function() use($app){

    verifyRequiredParams(array('CustomerID'));
    $db = new DbHandler();
    $response = array();
    
    $CustomerID = $app->request()->post("CustomerID");
    
    $result = $db->allProductsOnRouteOfCustomer($CustomerID);
    
    $response["error"] = false;
    $response["Products"] = array();

    // looping through result and preparing tasks array
    while ($products = $result->fetch_assoc()) {
        $tmp = array();
        $tmp["ProductID"] = $products["ProductID"];
        $tmp["CustomerID"] = $products["CustomerID"];
        $tmp["ProductName"] = $products["ProductName"];
        $tmp["ProductFrom"] = $products["ProductFrom"];
        $tmp["ProductTo"] = $products["ProductTo"];
        $tmp["ProductDetails"] = $products["ProductDetails"];
        array_push($response["Products"], $tmp);
    }

    echoRespnse(200, $response);
    
});

/* ------ */

$app->post('/location/saveLocation','authenticate', function() use($app){
    verifyRequiredParams(array('Latitude', 'Longitude', 'CarrierID'));
    $db = new DbHandler();
    $response = array();
    
    $Lat = $app->request()->post("Latitude");
    $Long = $app->request()->post("Longitude");
    $CarrierID = $app->request()->post("CarrierID");
    
    $result = $db->saveLocation($Lat, $Long, $CarrierID);
    if($result == true){
        $response["error"] = false;
        $response["message"] = "Location updated successfuly";
        $response["LocationID"] = $result;
        echoRespnse(201, $response);
    }elseif($result == false){
         $response["error"] = true;
        $response["message"] = "Location cannot be saved! Try again.";
        echoRespnse(200, $response);
    }else{
        $response["error"] = false;
        $response["message"] = "Location saved successfuly";
        $response["LocationID"] = $result;
        echoRespnse(201, $response);
    }

        
});

$app->post('/location/getLocation','authenticate', function() use($app){
    verifyRequiredParams(array('ProductID'));
    $db = new DbHandler();
    $response = array();
    
    $ProductID = $app->request()->post("ProductID");  
    $result = $db->getLocation($ProductID);
    

    if($result == false){
        $response["error"] = true;
        $response["message"] = "Location cannot be retrieved! Try again.";
    }else{
        $response["error"] = false;
        $response["message"] = "Location retrieved";
        $response["Location"] = array();
    
        while ($location = $result->fetch_assoc()) {
        $tmp = array();
        $tmp["Latitude"] = $location["CarrierLat"];
        $tmp["Longitude"] = $location["CarrierLong"];
        array_push($response["Location"], $tmp);
    }
    
    echoRespnse(201, $response);
    }

        
});


/**
 * Verifying required params posted or not
 */
function verifyRequiredParams($required_fields) {
    $error = false;
    $error_fields = "";
    $request_params = array();
    $request_params = $_REQUEST;
    // Handling PUT request params
    if ($_SERVER['REQUEST_METHOD'] == 'PUT') {
        $app = \Slim\Slim::getInstance();
        parse_str($app->request()->getBody(), $request_params);
    }
    foreach ($required_fields as $field) {
        if (!isset($request_params[$field]) || strlen(trim($request_params[$field])) <= 0) {
            $error = true;
            $error_fields .= $field . ', ';
        }
    }

    if ($error) {
        // Required field(s) are missing or empty
        // echo error json and stop the app
        $response = array();
        $app = \Slim\Slim::getInstance();
        $response["error"] = true;
        $response["message"] = 'Required field(s) ' . substr($error_fields, 0, -2) . ' is missing or empty';
        echoRespnse(400, $response);
        $app->stop();
    }
}

/**
 * Validating email address
 */
function validateEmail($email) {
    $app = \Slim\Slim::getInstance();
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $response["error"] = true;
        $response["message"] = 'Email address is not valid';
        echoRespnse(400, $response);
        $app->stop();
    }
}

/**
 * Echoing json response to client
 * @param String $status_code Http response code
 * @param Int $response Json response
 */
function echoRespnse($status_code, $response) {
    $app = \Slim\Slim::getInstance();
    // Http response code
    $app->status($status_code);

    // setting response content type to json
    $app->contentType('application/json');

    echo json_encode($response);
}

$app->get('/verification', function() {
    $response = array();
    $db = new DbHandler();
    
    // fetch task
    $UserID = $_GET['id'];
    $EmailCode = $_GET['code'];
    
    if (empty($UserID) && empty($EmailCode)) {
        //redirect user to main page!
    } else {
        $result = $db->VerifyEmail($UserID, $EmailCode);
    }

    if ($result != NULL) {
        echo $result["Message"];

    }else {
        $response["error"] = true;
        echoRespnse(404, $response);
        }
});

$app->run();
?>