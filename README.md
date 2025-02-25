# EATKG: An Open-Source Efficient <u>E</u>xact <u>A</u>lgorithm for the <u>T</u>wo-Dimensional <u>K</u>napsack Problem with <u>G</u>uillotine Constraints

This repository hosts the source code for the proposed algorithm, along with the corresponding instance data, aggregated results, and detailed solutions. The reference to our paper is provided below.

**Sunkanghong Wang, Roberto Baldacci, Qiang Liu, and Lijun Wei (2025) EATKG: An Open-Source Efficient <u>E</u>xact <u>A</u>lgorithm for the <u>T</u>wo-Dimensional <u>K</u>napsack Problem with <u>G</u>uillotine Constraints. Under Review.**

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
-Xms8G -Xmx8G -Djava.library.path=D:\WSKH\Environment\Cplex\Cplex_Library_And_Bin\Cplex1263\cplex\bin\x64_win64
```

where

-  **`-Xms8G`** sets the initial heap size to 8 GB.
-  **`-Xmx8G`** the maximum heap size to 8 GB.
-  **`-Djava.library.path=D:\WSKH\Environment\Cplex\Cplex_Library_And_Bin\Cplex1263\cplex\bin\x64_win64`** specifies the path to the native libraries for CPLEX, allowing JAVA to locate them for use.

## Instances

The **`Instances`** directory contains the **8** benchmark sets we used, and the format of each instance file is as follows:

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

- **Set**: .
