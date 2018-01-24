<h1 align="center">
  <br>
  <img width=20% src="https://raw.githubusercontent.com/tronprotocol/wiki/master/images/java-tron.png">
  <br>
  java-tron
  <br>
</h1>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
[![Gitter](https://img.shields.io/gitter/room/nwjs/nw.js.svg)](https://gitter.im/tronprotocol/java-tron)
[![Build Status](https://travis-ci.org/tronprotocol/java-tron.svg?branch=develop)](https://travis-ci.org/tronprotocol/java-tron)
[![GitHub issues](https://img.shields.io/github/issues/tronprotocol/java-tron.svg)](https://github.com/tronprotocol/java-tron/issues) 
[![GitHub pull requests](https://img.shields.io/github/issues-pr/tronprotocol/java-tron.svg)](https://github.com/tronprotocol/java-tron/pulls)
[![GitHub contributors](https://img.shields.io/github/contributors/tronprotocol/java-tron.svg)](https://github.com/tronprotocol/java-tron/graphs/contributors) 
[![license](https://img.shields.io/github/license/tronprotocol/java-tron.svg)](LICENSE)

# What's TRON?
TRON is a block chain-based decentralized smart protocol and an application development platform. It allows each user to freely publish, store and own contents and data, and in the decentralized autonomous form, decides an incentive mechanism and enables application developers and content creators through digital asset distribution, circulation and transaction, thus forming a decentralized content entertainment ecosystem.

TRON is a product of Web 4.0 and the decentralized internet of next generation.

# Quick Start

> Note: This repository is a IDEA project which you can simply download and import.

**Download and build**

```shell
> git clone https://github.com/tronprotocol/java-tron.git
> cd java-tron
> gradle build
```

**Import project to IDEA**

- [File] -> [New] -> [Project from Existing Sources...]
- Select java-tron/build.gradle
- Dialog [Import Project from Gradle], confirm [Use auto-import] and [Use gradle wrapper task configuration] have been
 selected，then select Gradle JVM（JDK 1.8）and click [OK]

# Testing

**Install Kafka and create two topics (block and transaction)**

**Update the configuration**


**Starting program**

IDEA: 
- [Edit Configurations...] -> [Add New Configuration] -> [Application]
- [Edit Configurations...] -> [Main Class]: `org.tron.program.example.Tron`
- [Edit Configurations...] -> [Use classpath of module]: `java-tron_main`
- [Edit Configurations...] -> [Program arguments]: `--type server`
- Run

![run](https://github.com/tronprotocol/wiki/blob/master/images/commands/default-set.gif)

or simply from terminal:
- ./gradlew run -Pserver=true

**Complete process**

![help](https://github.com/tronprotocol/wiki/blob/master/images/commands/process.gif)

**Other nodes to join need to modify the connection ip**
![help](https://github.com/tronprotocol/wiki/blob/master/images/commands/node-ip.gif)

# Commands
**help**

| Description | Example |
| --- | --- |
| Help tips | `help` |

![help](https://github.com/tronprotocol/wiki/blob/master/images/commands/help.gif)

**account**

| Description | Example |
| --- | --- |
| Get address | `account` |

![help](https://github.com/tronprotocol/wiki/blob/master/images/commands/account.gif)

**getbalance**

| Description | Example |
| --- | --- |
| Get balance | `getbalance` |

![help](https://github.com/tronprotocol/wiki/blob/master/images/commands/getbalance.gif)

**send [to] [balance]**

| Description | Example |
| --- | --- |
| Send balance to address | `send 2cddf5707aefefb199cb16430fb0f6220d460dfe 2` |

![help](https://github.com/tronprotocol/wiki/blob/master/images/commands/send1.gif)

**printblockchain**

| Description | Example |
| --- | --- | 
| Print blockchain | `printblockchain`|

![help](https://github.com/tronprotocol/wiki/blob/master/images/commands/printblockchain.gif)

**exit**

| Description | Example |
| --- | --- |
| Exit | `exit` |

![help](https://github.com/tronprotocol/wiki/blob/master/images/commands/exit.gif)

# Contact

Chat with us via [Gitter](https://gitter.im/tronprotocol/java-tron).

# Contribution
Contributions are welcomed and greatly appreciated. Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details on submitting patches and the contribution workflow.

