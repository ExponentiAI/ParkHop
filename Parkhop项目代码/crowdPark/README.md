# CrowdParkingLxw
基于google Map的停车位的寻找及上报应用。worker提供周边空的停车位信息，driver查看周边停车位信息并决定是否使用适合自己的停车位，选择后支付即可查看详细位置信息。

# 使用指南（功能介绍）
## 一、登录。两种身份：worker or driver
![image](https://github.com/XuewenLiao/CrowdParkingLxw/raw/master/images/login.jpg)  
## 二、worker提交停车位。worker回答周边POI问题、填写停车位描述信息、填写停车位价格后提交。
### 1、publish操作。点击右上方“publish”按钮。
![image](https://github.com/XuewenLiao/CrowdParkingLxw/raw/master/images/publish.jpg)  
### 2、定位。点击地图右上方定位图标定位到worker所在地。
![image](https://github.com/XuewenLiao/CrowdParkingLxw/raw/master/images/location.jpg)  
### 3、显示定位信息（该信息为停车位所在地地址）。点击“蓝色圆点”，显示出地址信息窗口。
![image](https://github.com/XuewenLiao/CrowdParkingLxw/raw/master/images/getaddress.jpg)  
### 4、提交停车位。填写停车位名称，driver需要支付的价格，同时回答POI问题，在认为正确的POI上打√。
![image](https://github.com/XuewenLiao/CrowdParkingLxw/raw/master/images/submit.jpg)  
## 三、driver使用停车位。
### 1、显示停车位信息列表。以driver身份登录后进入停车位列表，每一行代表一个停车位（由worker提供的），显示了停车位的名称、地址及价格。
![image](https://github.com/XuewenLiao/CrowdParkingLxw/raw/master/images/parkinglist.jpg)  
### 2、支付。提示框显示了所选择的停车位价格，点击“Yes”跳转（此处应该接入第三方支付或区块链点对点支付后跳转，未实现）。
![image](https://github.com/XuewenLiao/CrowdParkingLxw/raw/master/images/pay.jpg)  
### 3、显示停车位地点。
![image](https://github.com/XuewenLiao/CrowdParkingLxw/raw/master/images/show.jpg)

