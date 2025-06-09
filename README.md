# EATKG: An Open-Source Efficient <u>E</u>xact <u>A</u>lgorithm for the <u>T</u>wo-Dimensional <u>K</u>napsack Problem with <u>G</u>uillotine Constraints

This repository hosts the source code for the proposed algorithm, along with the corresponding instance data, aggregated results, and detailed solutions. The reference to our paper is provided below.

https://doi.org/10.1016/j.ejor.2025.05.033

**Sunkanghong Wang, Roberto Baldacci, Qiang Liu, and Lijun Wei (2025) EATKG: An Open-Source Efficient <u>E</u>xact <u>A</u>lgorithm for the <u>T</u>wo-Dimensional <u>K</u>napsack Problem with <u>G</u>uillotine Constraints. *European Journal of Operational Research*.**

If you have any questions, please feel free to reach out to **[villagerwei@gdut.edu.cn](mailto:villagerwei@gdut.edu.cn)** or **[wskh0929@gmail.com](mailto:wskh0929@gmail.com)**.

## Cite

To cite the contents of this repository, please cite both the paper and this repository.

Below is the BibTex for citing this repository:

```
@misc{wang2025eatkg,
  title={{EATKG}: {A}n Open-Source Efficient Exact Algorithm for the Two-Dimensional Knapsack Problem with Guillotine Constraints},
  author={Wang, Sunkanghong and Baldacci, Roberto and Liu, Qiang and Wei, Lijun},
  journal={European Journal of Operational Research},
  year={2025},
  publisher={Elsevier},
  doi={https://doi.org/10.1016/j.ejor.2025.05.033},
  url={https://github.com/WSKH0929/EATKG_For_2KPG},
  note={Available for download at https://github.com/WSKH0929/EATKG_For_2KPG},
}
```

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
```

## Code

In the **`Code/EATKG`** directory, you will find the source code of our algorithm (EATKG). We compiled and ran the code using the following software:

- IntelliJ IDEA 2024.3.3 (Compiler)
- Oracle OpenJDK 23.0.1
- Apache MAVEN 3.99

Users can run EATKG through **`src/main/java/com/wskh/run/RunSolver.java`**.

Notably, we installed the jar package of CPLEX into MAVEN so that we can use it directly by introducing the following dependency:

```xml
<dependency>
    <groupId>cplex</groupId>
    <artifactId>cplex</artifactId>
    <version>12.8.0</version>
</dependency>
```

The command used is as follows:

```shell
mvn install:install-file -Dfile=D:\WSKH\Environment\Cplex\Cplex_Library_And_Bin\Cplex1280\lib\cplex.jar -DgroupId=cplex -DartifactId=cplex -Dversion=12.8.0 -Dpackaging=jar
```

Users need to replace **`D:\WSKH\Environment\Cplex\Cplex_Library_And_Bin\Cplex1280\lib\cplex.jar`** with the file path of their own jar package of CPLEX.

Moreover, we used the following Virtual Machine Options to compile and run the proposed algorithm:

```shell
-Xms8G -Xmx8G -Djava.library.path=D:\WSKH\Environment\Cplex\Cplex_Library_And_Bin\Cplex1280\bin\x64_win64
```

where

-  **`-Xms8G`** sets the initial heap size to 8 GB.
-  **`-Xmx8G`** the maximum heap size to 8 GB.
-  **`-Djava.library.path=D:\WSKH\Environment\Cplex\Cplex_Library_And_Bin\Cplex1280\bin\x64_win64`** specifies the path to the native libraries for CPLEX, allowing JAVA to locate them for use.

## Instances

The **`Instances`** directory contains the instances in **8** benchmark sets we used, and the format of each instance file is as follows:

```shell
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

The **`Results`** directory contains results for two full versions (EATKG and EATKG (530s)) of the proposed algorithm as well as results for other versions.

We provide a csv file of the aggregated results for each version, where the columns have the following meanings:

- **set**: name of the benchmark set to which the instance belongs.
- **subset**: name of the benchmark subset to which the instance belongs.
- **instance**: name of the instance.
- **m**: number of item types.
- **n**: number of items.
- **W**: width of the container.
- **H**: height of the container.
- **sum_w**: total width of items.
- **sum_h**: total height of items.
- **m'**: number of item types after preprocessing.
- **n'**: number of items after preprocessing.
- **W'**: width of the container after preprocessing.
- **H'**: height of the container after preprocessing.
- **sum_w'**: total width of items after preprocessing.
- **sum_h'**: total height of items after preprocessing.
- **ub**: upper bound on the optimal solution value.
- **lb**: lower bound on the optimal solution value.
- **gap**: optimality gap, computed as (ub-lb)/ub.
- **opt**: whether a proven optimal solution has been found for the instance (yes: 1, no: 0).
- **OOM**: whether a memory overflow occur during the execution of the bidirectional tree search method (yes: 1, no: 0).
- **pre_time(s)**: time used for preprocessing (in seconds).
- **ub0_time(s)**: time used for computing the initial upper bound (in seconds).
- **dp_time(s)**: time used for the greedy DP heuristic (in seconds).
- **bid_time(s)**: time used for bidirectional tree search method (in seconds).
- **time(s)**: total time of algorithm execution (in seconds).

In particular, we provide the solution of each instance computed by the two full versions and its visualization, such as the solution visualization of GCUT13 below:

![GCUT13](https://picgo-wskh.oss-cn-guangzhou.aliyuncs.com/GCUT13.png)

where the white rectangles represent the packed items, the gray area represents the unused area in the container, and the numbers represent the id of the type of packed items (starting from 1).

Details of the solution to an instance can be found in the sol file, which has the following format:

```shell
isOpt LB UB Gap
n'
W H
t_i x_i y_i w_i h_i p_i (for each i=1,2,...,n')

where:
isOpt: is it a proven optimal solution? (true or false)
LB: solution value (total profit of packed items)
UB: upper bound on the optimal solution value
Gap: (UB-LB)/UB
n': number of packed items
W: width of the container
H: height of the container
i: item index
t_i: item type of the i-th item
x_i: x coordinate of the lower left corner of the i-th item
y_i: y coordinate of the lower left corner of the i-th item
w_i: width of the i-th item
h_i: height of the i-th item
p_i: profit of the i-th item
```
