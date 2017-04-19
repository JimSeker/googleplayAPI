<?php
 
class DbOperation
{
    //Database connection link
    private $con;
 
    //Class constructor
    function __construct()
    {
        //Getting the DbConnect.php file
        require_once dirname(__FILE__) . '/DbConnect.php';
 
        //Creating a DbConnect object to connect to the database
        $db = new DbConnect();
 
        //Initializing our connection link of this class
        //by calling the method connect of DbConnect class
        $this->con = $db->connect();
    }
 
    //storing token in database 
    public function registerDevice($name,$token){
        if(!$this->doesNameExist($name)){
            $stmt = $this->con->prepare("INSERT INTO devices (name, token) VALUES (?,?) ");
            $stmt->bind_param("ss",$name,$token);
            if($stmt->execute())
                return 0; //return 0 means success
            return 1; //return 1 means failure
        }else{
            return 2; //returning 2 means email already exist
        }
    }
 
    //the method will check if email already exist 
    private function doesNameExist($name){
        $stmt = $this->con->prepare("SELECT id FROM devices WHERE name = ?");
        $stmt->bind_param("s",$name);
        $stmt->execute();
        $stmt->store_result();
        $num_rows = $stmt->num_rows;
        $stmt->close();
        return $num_rows > 0;
    }
 
    //getting all tokens to send push to all devices
    public function getAllTokens(){
        $stmt = $this->con->prepare("SELECT token FROM devices");
        $stmt->execute(); 
       //same, if you have a msyldnd, instead of mysql you can uncomment these lines and remove the later.
       // $result = $stmt->get_result();
       //while($token = $result->fetch_assoc()){
       //   array_push($tokens, $token['token']);
       //  }
       // return $tokens; 

        $tokens = array(); 
        $stmt->store_result();
        $stmt->bind_result($token);     
        while($stmt->fetch() ) {
          $tokens[] = array("token"=>$token);
        } 
        return $tokens; 
    }

    //getting a specified token to send push to selected device
    public function getTokenByName($name){
      $stmt = $this->con->prepare("SELECT token FROM devices WHERE name = ?");
      $stmt->bind_param("s",$name);
      $stmt->execute(); 
      //if you have a mysqlnd server, then you can uncomment these two lines and remove the last 4.
      //$result = $stmt->get_result()->fetch_assoc();
      //return array($result['token']);        
      $stmt->store_result();
      $stmt->bind_result($token);     
      $stmt->fetch();
      return array($token);        
     
    }
 
    //getting all the registered devices from database 
    public function getAllDevices(){
        $stmt = $this->con->prepare("SELECT id, name, token FROM devices");
        $stmt->execute();
      //  $result = $stmt->get_result();
      //uncomment the above and comment everything to the return, if you have myslqnd instead of mysql.
        $stmt->store_result();
        $stmt->bind_result($id, $name, $token);     
        $result = array();
        while($stmt->fetch() ) {
          $result[] = array("id"=>$id, "name"=>$name, "token"=>$token);
        } 
        return $result; 
    }
}
