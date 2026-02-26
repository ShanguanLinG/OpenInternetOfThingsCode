import sys
import time
import json
from nle_library.httpHelp.NetWorkBusiness import NetWorkBusiness

import matplotlib.pyplot as plt

# 物联网应用开发赛题第1套 子任务2-6 广场气象系统

plt.rcParams['font.sans-serif'] = ['SimHei']
plt.rcParams['axes.unicode_minus'] = False


# 云服务类，使用了云服务的包进行获取数据
class CloudService:
    # 初始化，进行登录操作
    def __init__(self):
        self.net = NetWorkBusiness("www.nlecloud.com", 80)
        # 登录完毕后触发回调函数
        self.net.signIn("account", "account", self.cb)

    # 回调函数，设置访问令牌
    def cb(self, *args):
        self.net.setAccessToken(args[0]['ResultObj']['AccessToken'])

    # 实时获取云服务的新数据，apiTag为nl_temperature, nl_humidity
    # 不要使用自带的self.net.getSensor()方法，延迟非常高
    # 返回结果和时间的集合，方便进一步处理数据
    def get_new_data(self):
        while True:
            sensors = self.net.getSensorsRealTimeData("1270868", 1376219)
            data = {"nl_temperature": 0, "nl_humidity": 0, "time": time.strftime('%H:%M:%S', time.localtime())}
            for sensor in sensors['ResultObj']:
                api_tag = sensor['ApiTag']
                if "nl_temperature" == api_tag: data['nl_temperature'] = int(sensor['Value'])
                if "nl_humidity" == api_tag: data['nl_humidity'] = int(sensor['Value'])
            return data


# 绘画函数
def draw_chart():
    # 获取云服务类的实例
    cloud = CloudService()
    # 创建三个空列表以存放数据
    time_list = []
    temperature_list = []
    humidity_list = []
    # 开启交互模式
    plt.ion()
    # 创建一个图形窗口，其中包含 2 行 1 列的子图布局
    # fig: 整个图形窗口对象，可以控制整个画布（如大小、标题等）
    # (ax1, ax2): 包含两个子图对象的元组，对应两行子图
    # 2: 子图行数
    # 1: 子图列数
    # figsize=(8, 6): 设置图形窗口大小为 8 英寸宽 × 6 英寸高
    fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(8, 6))
    while True:
        # 获取云服务返回的实时数据
        new_data = cloud.get_new_data()
        # 填充列表中的数据
        time_list.append(new_data['time'])
        temperature_list.append(new_data['nl_temperature'])
        humidity_list.append(new_data['nl_humidity'])
        # 当数据大于10条时
        if len(time_list) > 10:
            # 移除每个列表中第一个的数据（即最旧的数据）
            time_list.pop(0)
            temperature_list.pop(0)
            humidity_list.pop(0)
        # 清空子图视图，让接下来的代码重新绘图，制造动态的效果
        ax1.clear()
        ax2.clear()
        # 开始子图1的绘图操作（核心）：x轴时间-y轴温度
        ax1.plot(time_list, temperature_list)
        # 设置子图1的标题
        ax1.set_title('温度-时间')
        # 设置子图1的x轴描述
        ax1.set_xlabel('时间')
        # 设置子图1的y轴描述
        ax1.set_ylabel('温度')
        # 开始子图2的绘图操作（核心）：x轴时间-y轴湿度
        ax2.plot(time_list, humidity_list)
        # 设置子图2的标题
        ax2.set_title('湿度-时间')
        # 设置子图2的x轴描述
        ax2.set_xlabel('时间')
        # 设置子图2的y轴描述
        ax2.set_ylabel('湿度')
        # 自动排版，让各个描述不会挤在一块
        plt.tight_layout()
        # 暂停1秒（每1秒读取一次数据，题目中要求是10秒，自行修改即可）
        plt.pause(1)
        # 如果图形窗口（fig对应的id）不存在，则退出循环
        if not plt.fignum_exists(fig.number):
            break
    # 关闭所有窗口
    plt.close('all')
    # 退出程序
    sys.exit(0)


if __name__ == '__main__':
    draw_chart()
