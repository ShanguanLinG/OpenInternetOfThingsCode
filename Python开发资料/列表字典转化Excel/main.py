import csv


class Data:
    def __init__(self):
        self.list = [
            ['姓名', '年龄', '城市', '工资'],
            ['张三', 25, '北京', 8000],
            ['李四', 30, '上海', 12000],
            ['王五', 28, '广州', 10000]
        ]
        self.dictionary = {
            "姓名": "张三",
            "年龄": 25,
            "城市": "北京",
            "工资": 8000
        }


if __name__ == '__main__':
    data = Data()
    with open('list.csv', 'w', newline='') as file:
        writer = csv.writer(file)
        writer.writerows(data.list)
    with open('dictionary.csv', 'w', newline='') as file:
        writer = csv.writer(file)
        writer.writerow(data.dictionary.keys())
        writer.writerow(data.dictionary.values())
