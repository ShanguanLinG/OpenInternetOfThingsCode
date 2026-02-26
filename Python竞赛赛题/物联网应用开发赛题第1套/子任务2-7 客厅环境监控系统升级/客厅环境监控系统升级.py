import sys
from nle_library.httpHelp.NetWorkBusiness import NetWorkBusiness

from PyQt5.QtCore import *
from PyQt5.QtWidgets import *


# 物联网应用开发赛题第1套 子任务2-7 客厅环境监控系统升级

# 自定义窗口类，继承QMainWindow
class MyWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        # 创建列表，包含原始数据
        data = [
            ['温度℃', '28'],
            ['温度%rh', '60'],
            ['光照度lux', '80'],
            ['红外对射', '有人'],
            ['常亮指示灯', 'On'],
        ]
        # 创建定时器类，用于定时执行代码
        self.timer = QTimer()
        # 将self.update方法绑定定时器
        self.timer.timeout.connect(self.update)
        # 每1秒更新一次数据（题目中要求是30秒，自行修改即可）
        self.timer.start(1000)
        # 创建一个表格对象，尺寸为5行2列（len(data)=5，即data列表的长度）
        self.table = QTableWidget(len(data), 2)
        # 设置不显示横向表头
        self.table.horizontalHeader().setVisible(False)
        # 设置不显示纵向表头
        self.table.verticalHeader().setVisible(False)
        # 开始填充初始数据
        for row, (key, value) in enumerate(data):
            # 每行的第一个填入传感器名称
            self.table.setItem(row, 0, QTableWidgetItem(str(key)))
            # 每行的第二个填入传感器数值
            self.table.setItem(row, 1, QTableWidgetItem(str(value)))
        # 设置主窗口的中心部件
        self.setCentralWidget(self.table)
        # 初始化变量，后面刷新后会覆盖
        self.nl_temperature = 0
        self.nl_humidity = 0
        self.z_light = 0
        self.z_body = 0
        # 创建云平台类实例
        self.net = NetWorkBusiness("www.nlecloud.com", 80)
        # 进行登录
        self.net.signIn("account", "password", self.cb)

    # 回调函数，代表登录成功后干什么事
    def cb(self, *args):
        # 设置访问令牌
        self.net.setAccessToken(args[0]['ResultObj']['AccessToken'])

    # 更新数据
    def update(self):
        sensors = self.net.getSensorsRealTimeData("1270868", 1376219)
        # 实时获取云服务的新数据
        for sensor in sensors['ResultObj']:
            api_tag = sensor['ApiTag']
            if "nl_temperature" == api_tag: self.nl_temperature = int(sensor['Value'])
            if "nl_humidity" == api_tag: self.nl_humidity = int(sensor['Value'])
            if "z_light" == api_tag: self.z_light = int(sensor['Value'])
            if "z_body" == api_tag: self.z_body = int(sensor['Value'])
        # 直接应用部分数据到表格中
        self.table.setItem(0, 1, QTableWidgetItem(str(self.nl_temperature)))
        self.table.setItem(1, 1, QTableWidgetItem(str(self.nl_humidity)))
        self.table.setItem(2, 1, QTableWidgetItem(str(self.z_light)))
        # 如果人体感应仪没有检测到人，则填充第4行2列的表格文字为“无人”
        if self.z_body == 0:
            self.table.setItem(3, 1, QTableWidgetItem(str("无人")))
        # 否则填充有人
        else:
            self.table.setItem(3, 1, QTableWidgetItem(str("有人")))
        # 如果光照度大于100
        if self.z_light > 100:
            # 关闭常亮指示灯
            self.net.control("1376219", "m_steady_white", 0.0)
            # 填充第5行第2列的表格文字为”Off“
            self.table.setItem(4, 1, QTableWidgetItem("Off"))
        else:
            # 开启常亮指示灯
            self.net.control("1376219", "m_steady_white", 1.0)
            # 填充第5行第2列的表格文字为”On“
            self.table.setItem(4, 1, QTableWidgetItem("On"))


if __name__ == '__main__':
    # 创建app实例，稍后用于启动
    app = QApplication(sys.argv)
    # 创建自定义窗口类
    window = MyWindow()
    # 展示窗口
    window.show()
    # 程序随着app的退出而退出
    sys.exit(app.exec_())
