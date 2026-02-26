import requests


class CloudService:
    # 初始化, 传入用户名和密码, 并对获取到的access_token进行存储
    def __init__(self, account, password):
        # 发送json数据的为包体请求
        json = {
            "Account": account,
            "Password": password
        }
        self.access_token = requests.post("http://api.nlecloud.com/Users/Login", json=json).json()['ResultObj'][
            'AccessToken']

    # 获取传感器数值
    def get_sensor_value(self, device_id, api_tag):
        # 请求直接处于URL中的是URL请求参数
        # header是请求头, 用于验证云平台账号
        headers = {
            "AccessToken": self.access_token
        }
        return requests.get(
            f"http://api.nlecloud.com/devices/{device_id}/Sensors/{api_tag}",
            headers=headers
        ).json()['ResultObj']['Value']

    # 控制执行器
    def control(self, device_id, api_tag, data):
        # params是URL请求参数, 与直接放在URL中一样
        # header是请求头, 用于验证云平台账号
        # data为请求体
        params = {
            "deviceId": device_id,
            "apiTag": api_tag
        }
        headers = {
            "AccessToken": self.access_token
        }
        requests.post("http://api.nlecloud.com/Cmds", params=params, json=data, headers=headers)


if __name__ == '__main__':
    cloud = CloudService("account", "password")
    print(cloud.get_sensor_value("1390229", "m_Infrared"))
    cloud.control("1376219", "nl_fan", 1)
