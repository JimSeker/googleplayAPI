<?php 
 
 require_once 'DbOperation.php';
 
 $db = new DbOperation(); 
 
 $devices = $db->getAllDevices();
 
 $response = array(); 
 
 $response['error'] = false; 
 $response['devices'] = array(); 
 
// while($device = $devices->fetch_assoc()){
 foreach ($devices as $device) {
   $temp = array();
   $temp['id']=$device['id'];
   $temp['name']=$device['name'];
   $temp['token']=$device['token'];
   array_push($response['devices'],$temp);
 }
 
 echo json_encode($response);
