# EATKG: An Open-Source Efficient <u>E</u>xact <u>A</u>lgorithm for the <u>T</u>wo-Dimensional <u>K</u>napsack Problem with <u>G</u>uillotine Constraints

This repository hosts the source code for the proposed algorithm, along with the corresponding instance data, aggregated results, and detailed solutions. The reference to our paper is provided below.

**Full reference: Sunkanghong Wang, Roberto Baldacci, Fabio Furini, Qiang Liu, and Lijun Wei (2025) EATKG: An Open-Source Efficient <u>E</u>xact <u>A</u>lgorithm for the <u>T</u>wo-Dimensional <u>K</u>napsack Problem with <u>G</u>uillotine Constraints. Under Review.**

If you have any questions, please feel free to reach out to **[villagerwei@gdut.edu.cn](mailto:villagerwei@gdut.edu.cn)** or **[wskh0929@gmail.com](mailto:wskh0929@gmail.com)**.

## Structure

The structure of the repository is shown below:

```shell
├─Code
│  └─EATKG
│      ├─.idea
│      ├─src
│      │  ├─main
│      │  │  ├─java
│      │  │  │  └─com
│      │  │  │      └─wskh
│      │  │  │          ├─classes
│      │  │  │          ├─components
│      │  │  │          ├─run
│      │  │  │          ├─solvers
│      │  │  │          └─utils
├─Instances
│  ├─Set1
│  ├─Set2
│  ├─Set3
│  ├─Set4
│  ├─Set5
│  ├─Set6
│  ├─Set7
│  └─Set8
├─Results
│  ├─Full_Version
│  │  ├─37-77-111
│  │  └─530
│  └─Other_Versions
└─Tables
```

## Code

In the **`Code/EATKG`** directory, you will find the source code of our algorithm (EATKG). We compiled and ran the code using the following software:

- IntelliJ IDEA 2024.3.3
- Oracle OpenJDK 23.0.1
- Apache MAVEN 3.99

Users can run EATKG through **`src/main/java/com/wskh/run/RunSolver.java`**.

Note that we installed the jar package of CPLEX into MAVEN so that we can use it directly by introducing the following dependency:

```xml
<dependency>
    <groupId>cplex</groupId>
    <artifactId>cplex</artifactId>
    <version>12.8.0</version>
</dependency>
```

## Instances

The **`Instances`** directory contains the **8** benchmark sets we used, and the format of each instance file is as follows:

```sheel
m
n
W H
w_t h_t p_t d_t (for each t=1,2,...,m)

where:
m: number of item types
n: number of available items
W: width of the container
H: height of the container
t: item type ID (starting from 1)
w_t: width of item type t
h_t: height of item type t
p_t: profit of item type t
d_t: copy number of item type t
```

## Results



- **Set**: .

## Tables
