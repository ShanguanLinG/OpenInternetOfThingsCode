# 打包时出现ModuleNotFoundError的解决方案

## 一. 问题描述

在使用PyInstaller打包Python程序时, 可能会出现`ModuleNotFoundError`错误, 这通常是因为某些依赖包没有被正确包含在打包文件中,
大概率是新大陆包的问题

## 二. 解决方案步骤

### 步骤1: 添加必要的import语句

在代码头部添加以下import语句(只要使用到nle_library包和tplink包都要添加, 不管实际代码有没有使用到):

```python
import json
import queue
import ctypes
import typing
```

### 步骤2: 移动依赖包到代码目录

将需要用到的包移动到代码同级目录下:

* 例如: `nle_library`
* 例如: `tplink`

### 步骤3: 使用正确的打包指令

使用以下PyInstaller指令进行打包:

```bash
pyinstaller.exe -Fw --add-binary "nle_library;nle_library" --add-binary 'tplink;tplink' <文件名>.py
```

## 三. 参数说明

### PyInstaller参数详解

| 参数             | 说明                       |
|----------------|--------------------------|
| `-F`           | 打包成单个可执行文件               |
| `-w`           | 使用窗口模式, 不显示控制台(适用于GUI程序) |
| `--add-binary` | 添加二进制文件或目录到打包文件中         |

### --add-binary参数格式

```
--add-binary '源路径;目标路径'
```

* **源路径**: 需要包含的包或文件的路径
* **目标路径**: 在打包文件中的目标路径
* **分隔符**: 使用分号`;`分隔源路径和目标路径

## 四. 示例说明

### 示例1: 包含单个包

如果只需要包含`nle_library`包:

```bash
pyinstaller.exe -Fw --add-binary "nle_library;nle_library" main.py
```

### 示例2: 包含多个包

如果需要包含多个包:

```bash
pyinstaller.exe -Fw --add-binary "nle_library;nle_library" --add-binary "tplink;tplink" --add-binary "other_package;other_package" main.py
```

## 五. 注意事项

1. 确保--add-binary后的参数名用双引号, 使用单引号可能会出现路径解析错误的情况发生: (Unable to find xxx when adding binary and data files.)