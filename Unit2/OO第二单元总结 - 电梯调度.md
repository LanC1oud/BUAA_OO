# OO第二单元总结 - 电梯调度

## 题目回顾

**week 1**：有 6 部电梯，在某些时间节点，会有一些指定重量 w 的乘客发出请求：乘坐指定电梯 e，从楼层 x 到楼层 y。当电梯开门，关门，到达任一楼层，乘客进出电梯时，需要输出特定的信息。每一部电梯的移动速度给定，同时电梯的开关门之间有一定的时间间隔。目标是将所有乘客送到目的地，且使得系统总运行时间，乘客优先级加权等待时间，电梯耗电量（与开关门次数和移动次数有关）最小化。

**week 2**：乘客不再指定乘坐某部电梯，而需要自行完成电梯调度系统，计算最优的接客策略。新增维修（MAINT）事件，需要在一定时间内调度特定电梯完成维修流程：MAINT-ACCEPT, MAINT1-BEGIN, MAINT2-BEGIN, MAINT-END，维修期间电梯上不能有乘客，被迫下电梯的乘客需要重新RECEIVE。

**week 3**：加入双桥厢电梯的升级（UPDATE）和回收（RECYCLE）流程，升级后一个电梯井内有两个电梯，7-12号负责B4-F2，1-6号负责F2-F7。回收后重新进入1-6号负责全楼层的状态。需要注意电梯不能同时进入F2.

## 程序整体架构

```Mermaid
graph TD
    Main["MainClass
• main() : Start & init all"]

    %% 输入
    Input["InputThread
• run() : Read input requests"]
    Req["Requests
• addRequest() : Add to queue
• getRequest() : Take from queue"]

    %% 调度
    Disp["Dispatcher
• run() : Dispatch loop
• dispatch() : Send request
• getBestElevator() : Choose best elevator
• calScore() : Compute score"]

    %% 核心
    Solver["Solver
• getInstr() : Decide next action
• canOpen() : Check open door"]
    EleTab["ElevatorTable
• addRequest() : Store request
• get/set : Manage state"]
    Ele["Elevator
• run() : Elevator main loop
• moveDirect() : Move to floor
• getIn() : Passenger in
• getOut() : Passenger out"]

    %% 专用处理器
    Maintainer["Maintainer
• handleMaintSequence() : Do maintenance"]
    Updater["Updater
• handleUpdateSequence() : Do upgrade"]
    Recycler["Recycler
• handleRecycleSequence() : Do recycle"]

    %% 互斥与工具
    Shaft["Shaft
• acquireF2() : Lock F2
• releaseF2() : Unlock F2"]
    Tool["Tool
• strToInt() : Floor to number
• intToStr() : Number to floor"]
    Instr["Instr : Action enum"]
    MyReq["MyPersonRequest : Passenger info"]

    %% 连线
    Main --> Input
    Main --> Disp
    Main --> Ele

    Input --> Req
    Req --> Disp
    Disp --> EleTab

    EleTab --> Solver
    Solver --> Instr
    EleTab --> Ele

    Ele --> Maintainer
    Ele --> Updater
    Ele --> Recycler
    Ele --> Shaft
    Ele --> Tool
```

### 架构分析

- MainClass:程序入口，全局初始化、启动并调度所有业务线程。
- InputThread:持续读取外部用户乘梯 / 特殊请求，封装后送入全局请求池。
- Dispatcher:全局调度器，为每一个新请求计算评分、匹配并分配最优电梯。
- Elevator:电梯独立工作线程，执行运行、开关门、人员进出、维护升级回收全流程动作。
  - Maintainer
  - Updater
  - Recycler  
- ElevatorTable:全局电梯状态中枢，集中维护所有电梯楼层、方向、内外呼等共享数据。
- Solver:电梯决策核心，根据实时状态计算并输出下一步最优运行指令。
- Requests:线程安全的全局请求队列，实现多线程间请求的生产与消费解耦。
- MyPersonRequest:自定义乘客请求实体，继承并扩展原生请求，补充额外业务字段。
- Tool:通用工具类，提供楼层格式转换、公共计算等静态辅助方法。
- Shaft:井道互斥控制类，实现 F2 楼层资源加锁释放，避免多轿厢通行冲突。


### 类复杂度分析

| 类名             | OCavg  | OCmax  | WMC    |
|------------------|--------|--------|--------|
| Dispatcher       | 5.17   | 9      | 31     |
| Elevator         | 3.80   | 14     | 38     |
| ElevatorTable    | 1.37   | 5      | 63     |
| InputThread      | 2.50   | 4      | 5      |
| Instr            | 0      | 0      | 0      |
| MainClass        | 2.00   | 2      | 2      |
| MyPersonRequest  | 1.00   | 1      | 3      |
| Requests         | 1.38   | 3      | 11     |
| Shaft            | 2.00   | 2      | 4      |
| Solver           | 6.80   | 13     | 34     |
| TestMain         | 1.00   | 1      | 1      |
| TestMain.T       | 4.67   | 8      | 14     |
| Tool             | 2.00   | 2      | 4      |
| **Total**        | -      | -      | **210**|
| **Average**      | **2.36** | **5.33** | **16.15** |

- **OCavg**：类内方法平均圈复杂度
- **OCmax**：类内方法最大圈复杂度
- **WMC**：类总加权方法复杂度（代表类整体复杂度）

### 方法复杂度分析

*分别统计时不展示get/set方法，计算Total和Average时计入。*

| 方法名 | CoGC | ev(G) | iv(G) | v(G) |
|--------|------|-------|-------|------|
| Elevator.run() | 24 | 3 | 16 | 18 |
| Solver.getInstr() | 21 | 11 | 12 | 20 |
| Solver.canOpen(int) | 16 | 8 | 14 | 19 |
| Solver.hasInsideTargetInDir(int, boolean) | 15 | 7 | 11 | 19 |
| Dispatcher.run() | 13 | 4 | 6 | 6 |
| Dispatcher.dispatch() | 11 | 6 | 8 | 9 |
| Dispatcher.getDistance(int, int, boolean, MyPersonRequest, Elevator) | 11 | 6 | 5 | 7 |
| Solver.hasReqInDir(int, boolean) | 11 | 5 | 5 | 11 |
| TestMain.TimableInputStream.read(byte[], int, int) | 10 | 8 | 2 | 10 |
| Elevator.getIn() | 9 | 6 | 5 | 8 |
| Elevator.moveDirect(long) | 9 | 5 | 7 | 9 |
| InputThread.run() | 9 | 3 | 4 | 5 |
| TestMain.TimableInputStream.read() | 9 | 4 | 3 | 6 |
| Dispatcher.getBestElevator(MyPersonRequest) | 7 | 3 | 6 | 7 |
| Elevator.getOut() | 7 | 1 | 6 | 7 |
| ElevatorTable.getWaitingFar(int, boolean) | 6 | 1 | 3 | 6 |
| Elevator.kickOutAll(String) | 5 | 1 | 0 | 4 |
| Requests.getRequest() | 5 | 3 | 4 | 5 |
| Dispatcher.calScore(ElevatorTable, MyPersonRequest) | 5 | 3 | 2 | 5 |
| ElevatorTable.addRequest(Request) | 4 | 1 | 5 | 5 |
| ElevatorTable.getNextFloor() | 4 | 2 | 1 | 4 |
| Shaft.acquireF2(int) | 4 | 3 | 2 | 4 |
| ElevatorTable.ElevatorTable(int, int, boolean) | 3 | 3 | 0 | 3 |
| ElevatorTable.getInsideFar() | 3 | 1 | 2 | 3 |
| ElevatorTable.removeFromLists(MyPersonRequest) | 3 | 2 | 0 | 4 |
| Tool.intToString(Integer) | 2 | 2 | 0 | 2 |
| Tool.strToInt(String) | 2 | 2 | 0 | 2 |
| Elevator.trySleep(long) | 1 | 1 | 1 | 2 |
| ElevatorTable.setEndFlag() | 1 | 1 | 2 | 2 |
| MainClass.main(String[]) | 1 | 1 | 2 | 2 |
| Requests.countMinus() | 1 | 1 | 0 | 2 |
| Requests.isOver() | 1 | 1 | 1 | 2 |
| Shaft.releaseF2(int) | 1 | 1 | 1 | 2 |
| Dispatcher.Dispatcher(Requests, ConcurrentHashMap<Integer, Elevator>) | 0 | 1 | 0 | 1 |
| Elevator.Elevator(Integer, Requests, ElevatorTable, ConcurrentHashMap) | 0 | 1 | 0 | 1 |
| Elevator.handleMaintSequence() | 0 | 1 | 0 | 1 |
| Elevator.handleRecycleSequence() | 0 | 1 | 0 | 1 |
| Elevator.handleUpdateSequence() | 0 | 1 | 0 | 1 |
| Requests.Requests() | 0 | 1 | 0 | 1 |
| Requests.addRequest(Request) | 0 | 1 | 0 | 1 |
| Requests.countPlus() | 0 | 1 | 0 | 1 |
| Requests.returnNull(boolean) | 0 | 1 | 0 | 1 |
| Requests.setEndFlag() | 0 | 1 | 0 | 1 |
| Solver.Solver(ElevatorTable) | 0 | 1 | 0 | 1 |
| TestMain.TimableInputStream(TimableInputStream) | 0 | 1 | 0 | 1 |
| TestMain.main(String[]) | 0 | 1 | 0 | 1 |

| 统计项 | CoGC | ev(G) | iv(G) | v(G) |
|--------|------|-------|-------|------|
| Total | 233 | 162 | 208 | 275 |
| Average | 2.62 | 1.82 | 2.34 | 3.09 |

- **CoGC**：圈复杂度总数
- **ev(G)**：基本复杂度，衡量代码结构化程度
- **iv(G)**：模块设计复杂度，衡量模块耦合度
- **v(G)**：圈复杂度，衡量代码分支、判断逻辑复杂程度

![alt text](image-2.png)

### 架构介绍

本电梯调度系统采用**多线程并发 + 分层解耦**架构设计，整体划分为输入接入层、全局调度层、核心业务执行层与公共支撑层，实现多电梯并行运行、智能请求分配、特殊运维流程处理及线程安全控制，具备高内聚、低耦合、易扩展的特点。

系统以`MainClass`作为统一入口，启动输入、调度及多电梯独立工作线程。`InputThread`负责读取外部乘客请求与运维指令，封装后存入线程安全的`Requests`队列，实现请求生产与消费解耦。`Dispatcher`作为全局调度核心，通过评分算法动态选择最优电梯，将请求高效分发至对应电梯实例。

核心执行层以`Elevator`线程为主体，每台电梯独立运行；`ElevatorTable`集中管理电梯状态，包括楼层、方向、内外请求及运维标记，保证全局数据一致性；`Solver`作为决策单元，依据实时状态生成移动、开关门、换向、维护等指令，驱动电梯执行动作；`Shaft`实现楼层互斥控制，避免多轿厢资源竞争冲突。

公共支撑层提供工具转换、请求实体扩展与指令枚举等基础能力，支撑全系统稳定运行。整体架构通过线程安全设计、模块化分工与智能决策机制，满足高并发场景下电梯高效、安全、稳定调度的需求。

#### 一、架构优势

1. 分层解耦，职责清晰
系统采用**四层分层架构**（输入接入层、全局调度层、核心业务层、公共支撑层），各模块职责单一且依赖关系明确。

2. 多线程并发，高效并行
基于多线程并发模型设计，实现全系统并行运行。

3. 智能调度，决策精准
调度层`Dispatcher`通过`calScore()`评分算法，综合电梯位置、方向、负载等维度，动态匹配最优电梯，提升调度效率。

### 二、优化空间

1. 完善日志与监控体系
加入Debug类输出日志，分级存储普通运行日志、异常日志、调试日志，便于调试。

2. 拆分复杂方法，降低圈复杂度
针对`Elevator.run()`、`Solver.getInstr()`等高复杂度核心方法：

- 拆分单一职责方法，如将“电梯移动逻辑”拆分为`moveUp()`、`moveDown()`，降低代码冗余与复杂度；
- 引入设计模式（如策略模式、工厂模式）优化复杂分支逻辑，提升代码可读性与可维护性。

## 架构设计体验

### week 1

第一周的作业是初步建立一个六部电梯的系统，能完成被输入指定的电梯调度行为，为了性能的优化，需要掌握以下两方面知识：1.多线程的基本知识，如线程的创建，线程的互斥与协作，wait/notify机制。2.生产者-消费者模式。

多线程知识：

- 创建线程主要有两种方式：
  
  - 继承`Thread`类，重写`run()`方法，但由于Java是单继承，继承了Thread就不能继承其他类了。
  - 更常用的是实现`Runnable`接口，实现`run()`方法，然后将其作为参数传递给Thread对象。这样做解耦了任务和线程，可以继承其他类

  ```Java
  Thread t = new Thread(() -> System.out.println("任务运行中"));
  t.start();
  ```

- 线程的互斥：当多个线程访问同一个共享资源（如全局变量）时，会产生竞态条件，导致数据错乱。

  - `synchronized`关键字：同步代码块或同步方法。每个Java对象头里都有一个Monitor，线程进入时必须获取这个锁，一般会将需要改变全局变量的代码块或方法设为`synchronized`的。

- 线程的协作：

  - `wait()`/`notify()`机制：这些方法属于`Object`类，`wait()`会使线程释放它持有的所，并进入等待状态。`notify()`/`notifyAll()`会唤醒在该对象监视器上等待的线程。
  - 必须在`synchronized`中使用，否则会抛出异常。并且需要在while循环中使用`wait()`，否则可能线程被唤醒了但条件其实还没满足。
  
  ```Java
  synchronized (lock) {
    while (resourceCount == 0) {
        lock.wait(); // 释放锁，等待
    }
    // 消费资源...
    lock.notifyAll(); // 通知生产者
  }
  ```

生产者-消费者模式：多线程协作中最经典的设计模式。通过一个中间容器解耦“产生数据的线程”和“处理数据的线程”，解决由于双方处理速度不匹配导致的阻塞问题。

实现方式：

- 使用`wait()`和`notifyAll()`，形如：

```Java
static List<Integer> queue = new ArrayList<>(); // 缓冲区

    public static void main(String[] args) {
        // 生产者
        new Thread(() -> {
            while (true) synchronized (queue) {
                while (queue.size() == 1) try { queue.wait(); } catch (Exception e) {}
                queue.add(1);
                System.out.println("生产");
                queue.notifyAll(); // 通知消费者
            }
        }).start();

        // 消费者
        new Thread(() -> {
            while (true) synchronized (queue) {
                while (queue.isEmpty()) try { queue.wait(); } catch (Exception e) {}
                queue.remove(0);
                System.out.println("消费");
                queue.notifyAll(); // 通知生产者
            }
        }).start();
    }
```

有了这两个前置知识，基础电梯调度系统的设计便十分流程化了。

建立`Elevator`线程和`Dispatcher`线程（实际设计中为了功能解耦与可读性强，分出了许多副类），由`Dispatcher`接收与分析请求，将请求解析后发送给特定的`Elevator`线程，再让`Elevator`进行MOVE,REVERSE,OPENCLOSE,OVER等行为。在电梯运行过程中使用**LOOK算法**：提前看好前方方向是否还有请求，如果没有则掉头。这样可以有效减少无效路程，提高性能。

### week 2

第二次作业新增维修请求，以及电梯自由调度功能（即不再由乘客选择电梯，而采取最优分配策略）。

先考虑维修请求：题目中对维修功能的描述十分复杂，单通篇看完后不难发现维修请求其实类似CO课中CPU的**中断机制**：记录下现有状态，跳转进入其他任务，在其他任务完成后复原先前记录下的状态，继续进行任务。维修的实际流程其实并不影响主任务的状态。不妨先采取最暴力的策略：在接收到MAINT请求时，立刻开门将所有人踢出电梯，火速开始执行MAINT任务，维修结束后一切照常。这听起来十分轻松好实现，因为维修过程其实是一个严格顺序执行的线性流程。但实际在“开门将所有人踢出电梯”时需注意许多细节，同时对`insideList`,`waitingList`,`toList`,`fromList`等多个队列进行同时维护时，一定反复检查是否每个队列都受到了严格正确操作，否则很容易出现请求被复制的情况。

接下来开始进行优化：根据维修检查要去的楼层，可以计算出前往维修路上最多需要几个行动周期（0.4s），考虑时间抖动流出至少一个周期的富余后，可以求解出较优的优化策略，我的代码中采取了相对直观，但仍有优化空间的策略：仍旧在接收到MAINT指令后第一时间往F1方向走，但并不第一时间踢出所有人，而是分为三部分：将一部分人踢出，一部分人送往顺道路过的正确楼层，一部分送往F1。这样已经可以做到较好优化。此处队列维护更加复杂，因此可以将每次上下人队列维护的代码封装成方法，避免遗漏。

接下来考虑电梯自由调度功能。在`Dispachter`类中建立专门的`getBestElevator`方法用于确定最优电梯，其中借鉴简化版的影子电梯思想，调整distance，weight，floor等变量的参数，近似模拟一个电梯评分公式，取最高分电梯进行计算。不过这种方法只能大致确定策略，无法做到最优。

### week 3

第三次作业新增升级与回收请求，以及双桥厢功能。

此次任务相对比较简单，UPDATE和RECYCLE指令的流程与MAINT的十分类似，几乎可以复用MAINT的代码，只进行简单的修改。本次作业的重点不在于对指令的输出反馈，而在于双桥厢电梯逻辑的实现。初步构想十分简单：当电梯进入DOUBLE状态时，改变相应电梯的`UpBound`和`DownBound`即可，使其无法到达范围外楼层，再改写电梯分配规则，降低需要穿过F2的请求的双桥厢电梯权重即可。实际实现时F2的电梯不能共存是一个难点（很多情况下，甚至会出现两个电梯在一个完全相同的时间戳想要进入F2），使用一个对F2单独的锁最保险也最简单的写法：

```Java
public class Shaft {
    private int currentOnF2 = 0;
    
    public synchronized void acquireF2(int type) {
        while (currentOnF2 != 0 && currentOnF2 != type) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        currentOnF2 = type;
    }
    
    public synchronized void releaseF2(int type) {
        if (currentOnF2 == type) {
            currentOnF2 = 0;
            notifyAll();
        }
    }
}
```

类似于理论课的PV信号量进行互斥的思想，在合适的位置加入PV指令即可。

## bug分析

三次作业大部分时间我都在跟神秘的`CPU_TIME_LIMIT_EXCEED`和`REAL_TIME_LIMIT_EXCEED`的问题作斗争，究其原因是对多线程与加锁的理解不足，导致出现死锁问题或CPU轮询。加锁有一下注意事项：

- 控制锁粒度，降低阻塞范围：仅对共享资源读写代码加锁，缩小锁覆盖范围，减少线程竞争，提升系统并发能力。
- 避免锁嵌套引发死锁：统一多锁获取顺序，禁止循环等待；不随意嵌套持有不同对象锁，防止线程死锁。
- 锁内避免执行耗时操作：锁代码块中尽量不使用 sleep()与长循环，避免长期占用锁导致性能下降。
- **不要在锁内sleep！**

逻辑上，大部分bug是由于代码初期建立了太多复杂的ArrayList维护，其中不乏许多业务逻辑重复，实际可以消减的。过于复杂和重复的List导致维护困难，很容易漏接，漏放，重复接，重复放。在第二次作业后我重新整理了List的使用，此后就没有出现过此类问题。

第三次作业中对F2的竞争逻辑出过一些小瑕疵，但确定了加锁与释放机制后就没什么问题了。

## hack策略

无论自测还是互测，主要都分为几个步骤：先进行黑盒测试，测试常见的基础功能；利用评测机测试相对复杂的随机大数据，验证鲁棒性，重点测试高并发情况下的线程错误；思考可能出现的极端情况进行测试，如超重和全UPDATE，全MAINTAIN；最后看代码白盒分析，直接通过代码寻找可能的逻辑漏洞或性能缺陷。

在hack过程中，使用大模型辅助生成了数据生成器和对拍器，但可惜没有通过纯随机的数据生成hack出什么bug。

## 优化分析

本单元作业的优化主要集中在第二次作业后的分配策略上，主要思路分为电梯打分制和影子电梯。我采用的是打分制，即当收到请求时，对于每个电梯，综合距离，电梯内人数，电梯内重量，电梯内每个人的toFloor等计算分数，然后让分数最高的电梯RECEIVE请求。其中的关键在于对每个因素权重的设计，这个方法的缺点也体现在这里：每个参数的权重比较凭感觉，设置的权重未必是数学上最合理的。并且实际情况电梯内可能有多个人去往不同方向不同楼层，十分复杂，不好量化。最终我决定给目前请求fromFloor和电梯currentFloor之间距离，以及电梯内人数较高的权重，而没有考虑电梯内每个人的具体数据。实际上这显然不是最优。

另一个思路是影子电梯，即对每个电梯模拟一次接了这个请求之后的运行情况。由于电梯层数并不多，每个电梯人数也不会太大，因此不至于消耗很大算力。这个方法的优点显然：一定能算出目前状态下的局部最优解。但是由于此刻不知道以后会有什么请求，因此也无法做到全局的最优。

实际我认为全局的最优是无法实现的，毕竟请求不是同时到达。影子电梯应当是更优的算法，但我最后还是采取了实现更简单的打分制，或许有机会可以试试影子电梯。

week3后追加：打分制中，若此请求将要穿过F2，则给予双桥厢电梯相当大的惩罚，尽量不使用双桥厢换乘机制。

出去电梯分配外，还有一些边角的优化空间：收到特殊请求后能否充分利用其后6s/7s的时间做更多事，这个用影子电梯也可以寻找到局部最优，但我最后选择了更易实现的方法（详见上week2分析）。

## 大模型使用心得

本次作业主要在第一次作业使用了Gemini模型。由于对生产者消费者模式有点一头雾水，拿到题目时完全无从下手，于是参考了往届代码借助AI搭好了大概框架，再根据AI写的代码理解多线程与生产者消费者模式的学习。在第三次作业中，既然`Updater`和`Recycler`可以复用`Maintainer`的代码，我索性直接让AI根据我的`Maintainer`改出了另两个类（更改变量名与状态Status之类的），然后再自己插入双轿厢电梯的改变逻辑。

## 心得体会

### 多线程

多线程相比原本的单线程听起来是个很大的跨越，也确实给我初期的代码编写造成了很大的理解阻碍。但是写完三次作业后发现实际上并没有很大的代码编写思路的变化，实际只需要注意在使用共享变量时加锁就好了。至于锁的使用有许多很细节的安全性问题，但都不会造成很大的理解障碍。一切多线程都是纸老虎！（）

### 生产者-消费者模式

经过了OO和OS的共同学习，对生产者-消费者模式有了更深入的理解，最大的感触不是电梯调度具体怎么编写，而是这种模式，以及线程互斥/并行的思路可以延申到许多实际项目的编写中。可以说这是我前两个单元OO学习中最大的收获。

### 层次化设计与线程安全

层次化设计：

- 输入层（InputThread）：专门负责解析控制台输入，生成 PersonRequest 对象。
- 共享数据层（WaitingList）：作为缓冲区，充当托盘。
- 调度层（Dispatcher）：调度电梯分配
- 执行层（ElevatorThread）：电梯只盯着托盘，有请求就动，没请求就等（wait）。

优秀的层次化设计能解决绝大部分线程安全问题：

- 明确了锁的范围，只有在跨层传递数据时，才需要加锁。
- 根绝死锁的可能，各层级之间不存在“平级互相调用”，锁的获取顺序被严格固定，永远是上层往底层拿锁。
- 通过层次化，线程结束标志可以逐层传递，优雅的终止线程。

## 个人感受和建议

- 希望在指导书中加入跳转...现在读指导书稍微有点费劲，经常为了看懂一个新功能的全部机制与约束需要到处找。
- 官方包的使用有点死板，希望可以用更灵活的方式定义官方类。
- 希望给这个单元加一个输出检查了，方便个人核对输出。
