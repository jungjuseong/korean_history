
# Chapter 17. Working with Large Databases

관계형 데이터베이스의 초기에는 하드 드라이브 용량이 메가바이트 단위로 측정되었으며 데이터베이스는 일반적으로 너무 커질 수 없기 때문에 관리하기 쉽습니다. 그러나 오늘날에는 하드 드라이브 용량이 15TB로 증가했으며 최신 디스크 어레이는 4PB 이상의 데이터를 저장할 수 있으며 클라우드의 스토리지는 기본적으로 무제한입니다. 관계형 데이터베이스는 데이터 볼륨이 계속 증가함에 따라 다양한 문제에 직면하지만 기업이 여러 스토리지 계층 및 서버에 데이터를 분산하여 관계형 데이터베이스를 계속 활용할 수 있도록 하는 파티셔닝, 클러스터링 및 샤딩과 같은 전략이 있습니다. 다른 기업들은 엄청난 양의 데이터를 처리하기 위해 Hadoop과 같은 빅 데이터 플랫폼으로 이동하기로 결정했습니다. 이 장에서는 관계형 데이터베이스를 확장하는 기술에 중점을 두고 이러한 전략 중 일부를 살펴봅니다.

## Partitioning

데이터베이스 테이블은 정확히 언제 "너무 커집니까"? 10명의 다른 데이터 설계자/관리자/개발자에게 이 질문을 하면 10가지 다른 대답을 얻을 수 있습니다. 그러나 대부분의 사람들은 다음 작업이 테이블이 수백만 행을 넘어 성장함에 따라 더 어렵고 시간이 오래 걸린다는 데 동의할 것입니다.

- 전체 테이블 스캔이 필요한 쿼리 실행
- 인덱스 생성/재구축
- 데이터 보관/삭제
- 테이블/인덱스 통계 생성
- 테이블 재배치(예: 다른 테이블스페이스로 이동)
- 데이터베이스 백업

이러한 작업은 데이터베이스가 작을 때 일상적으로 시작한 다음 더 많은 데이터가 축적됨에 따라 시간이 많이 걸리고 제한된 관리 시간 창으로 인해 문제가 되거나 불가능해질 수 있습니다. 향후 관리 문제가 발생하지 않도록 하는 가장 좋은 방법은 테이블을 처음 만들 때 큰 테이블을 조각 또는 파티션으로 나누는 것입니다(테이블은 나중에 파티션할 수 있지만 처음에는 더 쉽습니다). 관리 작업은 개별 파티션에서 종종 병렬로 수행될 수 있으며 일부 작업은 하나 이상의 파티션을 완전히 건너뛸 수 있습니다.

## Partitioning 개념

테이블 파티셔닝은 1990년대 후반 Oracle에 의해 도입되었지만 그 이후로 모든 주요 데이터베이스 서버에는 테이블과 인덱스를 파티셔닝하는 기능이 추가되었습니다. 테이블이 분할되면 두 개 이상의 테이블 파티션이 생성되며 각각은 정확히 동일한 정의를 갖지만 데이터의 하위 집합이 겹치지 않습니다. 

예를 들어 판매 데이터가 포함된 테이블은 판매 날짜가 포함된 열을 사용하여 월별로 분할하거나 시/도 코드를 사용하여 지역별로 분할할 수 있습니다.

테이블이 분할되면 테이블 자체가 가상 개념이 됩니다. 파티션은 데이터를 보유하고 모든 인덱스는 파티션의 데이터에 구축됩니다. 그러나 데이터베이스 사용자는 테이블이 분할되었다는 사실을 모른 채 여전히 테이블과 상호 작용할 수 있습니다. 이는 사용자가 실제 테이블이 아닌 인터페이스인 스키마 개체와 상호 작용한다는 점에서 뷰와 개념이 유사합니다. 모든 파티션에는 동일한 스키마 정의(열, 열 유형 등)가 있어야 하지만 각 파티션마다 다를 수 있는 몇 가지 관리 기능이 있습니다.

- 파티션은 다른 물리적 스토리지 계층에 있을 수 있는 다른 테이블스페이스에 저장될 수 있습니다.
- 파티션은 다른 압축 체계를 사용하여 압축할 수 있습니다.
일부 파티션의 경우 로컬 인덱스(자세한 ​​내용은 곧 설명)를 삭제할 수 있습니다.
- 테이블 통계는 일부 파티션에서 고정될 수 있으며 다른 파티션에서는 주기적으로 새로 고쳐집니다.
- 개별 파티션을 메모리에 고정하거나 데이터베이스의 플래시 스토리지 계층에 저장할 수 있습니다.

따라서 테이블 파티셔닝은 데이터 저장 및 관리에 유연성을 허용하는 동시에 단일 테이블의 단순성을 사용자 커뮤니티에 제공합니다.

### Table Partitioning
The partitioning scheme available in most relational databases is horizontal partitioning, which assigns entire rows to exactly one partition. Tables may also be partitioned vertically, which involves assigning sets of columns to different partitions, but this must be done manually. When partitioning a table horizontally, you must choose a partition key, which is the column whose values are used to assign a row to a particular partition. In most cases, a table’s partition key consists of a single column, and a partitioning function is applied to this column to determine in which partition each row should reside.

### Index Partitioning
If your partitioned table has indexes, you will get to choose whether a particular index should stay intact, known as a global index, or be broken into pieces such that each partition has its own index, which is called a local index. Global indexes span all partitions of the table and are useful for queries that do not specify a value for the partition key. For example, let’s say your table is partitioned on the sale_date column, and a user executes the following query:
```sql
SELECT sum(amount) FROM sales WHERE geo_region_cd = 'US'
```
Since this query does not include a filter condition on the sale_date column, the server will need to search every partition in order to find the total US sales. If a global index is built on the geo_region_cd column, however, then the server could use this index to quickly find all of the rows containing US sales.

## Partitioning Methods

While each database server has its own unique partitioning features, the next three sections describe the common partitioning methods available across most servers.

### Range partitioning
Range partitioning was the first partitioning method to be implemented, and it is still one of the most widely used. While range partitioning can be used for several different column types, the most common usage is to break up tables by date ranges. For exmple, a table named sales could be partitioned using the sale_date column such that data for each week is stored in a different partition:

```sql
CREATE TABLE sales
     (sale_id INT NOT NULL,
      cust_id INT NOT NULL,
      store_id INT NOT NULL,
      sale_date DATE NOT NULL,
      amount DECIMAL(9,2)
     )
    PARTITION BY RANGE (yearweek(sale_date))
     (PARTITION s1 VALUES LESS THAN (202002),
      PARTITION s2 VALUES LESS THAN (202003),
      PARTITION s3 VALUES LESS THAN (202004),
      PARTITION s4 VALUES LESS THAN (202005),
      PARTITION s5 VALUES LESS THAN (202006),
      PARTITION s999 VALUES LESS THAN (MAXVALUE)
     );
```
```
Query OK, 0 rows affected (1.78 sec)
```
이 문은 2020년의 처음 5주마다 하나씩 6개의 다른 파티션을 생성하고 2020년 5주 이후의 모든 행을 보유하기 위해 s999라는 여섯 번째 파티션을 생성합니다. 이 테이블의 경우 yearweek(sale_date) 표현식이 파티셔닝 함수로 사용됩니다. , 그리고 sale_date 열은 파티션 키로 사용됩니다. 파티션된 테이블에 대한 메타데이터를 보려면 information_schema 데이터베이스의 파티션 테이블을 사용할 수 있습니다.

```sql
SELECT partition_name, partition_method, partition_expression
    FROM information_schema.partitions
    WHERE table_name = 'sales'
    ORDER BY partition_ordinal_position;
```
```
+----------------+------------------+-------------------------+
| PARTITION_NAME | PARTITION_METHOD | PARTITION_EXPRESSION    |
+----------------+------------------+-------------------------+
| s1             | RANGE            | yearweek(`sale_date`,0) |
| s2             | RANGE            | yearweek(`sale_date`,0) |
| s3             | RANGE            | yearweek(`sale_date`,0) |
| s4             | RANGE            | yearweek(`sale_date`,0) |
| s5             | RANGE            | yearweek(`sale_date`,0) |
| s999           | RANGE            | yearweek(`sale_date`,0) |
+----------------+------------------+-------------------------+
6 rows in set (0.00 sec)
```
One of the administrative tasks that will need to be performed on the sales table involves generating new partitions to hold future data (to keep data from being added to the maxvalue partition). Different databases handle this in different ways, but in MySQL you could use the reorganize partition clause of the alter table command to split the s999 partition into three pieces:
```sql
ALTER TABLE sales REORGANIZE PARTITION s999 INTO
 (PARTITION s6 VALUES LESS THAN (202007),
  PARTITION s7 VALUES LESS THAN (202008),
  PARTITION s999 VALUES LESS THAN (MAXVALUE)
 );
```
If you execute the previous metadata query again, you will now see eight partitions:

```sql
SELECT partition_name, partition_method, partition_expression
    FROM information_schema.partitions
    WHERE table_name = 'sales'
    ORDER BY partition_ordinal_position;
```
```
+----------------+------------------+-------------------------+
| PARTITION_NAME | PARTITION_METHOD | PARTITION_EXPRESSION    |
+----------------+------------------+-------------------------+
| s1             | RANGE            | yearweek(`sale_date`,0) |
| s2             | RANGE            | yearweek(`sale_date`,0) |
| s3             | RANGE            | yearweek(`sale_date`,0) |
| s4             | RANGE            | yearweek(`sale_date`,0) |
| s5             | RANGE            | yearweek(`sale_date`,0) |
| s6             | RANGE            | yearweek(`sale_date`,0) |
| s7             | RANGE            | yearweek(`sale_date`,0) |
| s999           | RANGE            | yearweek(`sale_date`,0) |
+----------------+------------------+-------------------------+
8 rows in set (0.00 sec)
```
Next, let’s add a couple of rows to the table:

```sql
INSERT INTO sales
    VALUES
     (1, 1, 1, '2020-01-18', 2765.15),
     (2, 3, 4, '2020-02-07', 5322.08);
```
```
Query OK, 2 rows affected (0.18 sec)
Records: 2  Duplicates: 0  Warnings: 0
```
The table now has two rows, but into which partitions were they inserted? To find out, let’s use the partition subclause of the from clause to count the number of rows in each partition:

```sql
SELECT concat('# of rows in S1 = ', count(*)) partition_rowcount
    FROM sales PARTITION (s1) UNION ALL
    SELECT concat('# of rows in S2 = ', count(*)) partition_rowcount
    FROM sales PARTITION (s2) UNION ALL
    SELECT concat('# of rows in S3 = ', count(*)) partition_rowcount
    FROM sales PARTITION (s3) UNION ALL
    SELECT concat('# of rows in S4 = ', count(*)) partition_rowcount
    FROM sales PARTITION (s4) UNION ALL
    SELECT concat('# of rows in S5 = ', count(*)) partition_rowcount
    FROM sales PARTITION (s5) UNION ALL
    SELECT concat('# of rows in S6 = ', count(*)) partition_rowcount
    FROM sales PARTITION (s6) UNION ALL
    SELECT concat('# of rows in S7 = ', count(*)) partition_rowcount
    FROM sales PARTITION (s7) UNION ALL
    SELECT concat('# of rows in S999 = ', count(*)) partition_rowcount
    FROM sales PARTITION (s999);
```
```
+-----------------------+
| partition_rowcount    |
+-----------------------+
| # of rows in S1 = 0   |
| # of rows in S2 = 1   |
| # of rows in S3 = 0   |
| # of rows in S4 = 0   |
| # of rows in S5 = 1   |
| # of rows in S6 = 0   |
| # of rows in S7 = 0   |
| # of rows in S999 = 0 |
+-----------------------+
8 rows in set (0.00 sec)
```
The results show that one row was inserted into partition S2, and the other row was inserted into the S5 partition. The ability to query a specific partition involves knowing the partitioning scheme, so it is unlikely that your user community will be executing these types of queries, but they are commonly used for administrative types of activities.

List partitioning
If the column chosen as the partitioning key contains state codes (e.g., CA, TX, VA, etc.), currencies (e.g., USD, EUR, JPY, etc.), or some other enumerated set of values, you may want to utilize list partitioning, which allows you to specify which values will be assigned to each partition. For example, let’s say that the sales table includes the column geo_region_cd, which contains the following values:
```
+---------------+--------------------------+
| geo_region_cd | description              |
+---------------+--------------------------+
| US_NE         | United States North East |
| US_SE         | United States South East |
| US_MW         | United States Mid West   |
| US_NW         | United States North West |
| US_SW         | United States South West |
| CAN           | Canada                   |
| MEX           | Mexico                   |
| EUR_E         | Eastern Europe           |
| EUR_W         | Western Europe           |
| CHN           | China                    |
| JPN           | Japan                    |
| IND           | India                    |
| KOR           | Korea                    |
+---------------+--------------------------+
13 rows in set (0.00 sec)
```
You could group these values into geographic regions and create a partition for each one, as in:

```sql
CREATE TABLE sales
     (sale_id INT NOT NULL,
      cust_id INT NOT NULL,
      store_id INT NOT NULL,
      sale_date DATE NOT NULL,
      geo_region_cd VARCHAR(6) NOT NULL,
      amount DECIMAL(9,2)
     )
    PARTITION BY LIST COLUMNS (geo_region_cd)
     (PARTITION NORTHAMERICA VALUES IN ('US_NE','US_SE','US_MW',
                                        'US_NW','US_SW','CAN','MEX'),
      PARTITION EUROPE VALUES IN ('EUR_E','EUR_W'),
      PARTITION ASIA VALUES IN ('CHN','JPN','IND')
     );
```
```
Query OK, 0 rows affected (1.13 sec)
```
The table has three partitions, where each partition includes a set of two or more geo_region_cd values. Next, let’s add a few rows to the table:

```sql
INSERT INTO sales
    VALUES
     (1, 1, 1, '2020-01-18', 'US_NE', 2765.15),
     (2, 3, 4, '2020-02-07', 'CAN', 5322.08),
     (3, 6, 27, '2020-03-11', 'KOR', 4267.12);
```
```
ERROR 1526 (HY000): Table has no partition for value from column_list
It looks like there was a problem, and the error message indicates that one of the geographic region codes was not assigned to a partition. Looking at the create table statement, I see that I forgot to add Korea to the asia partition. This can be fixed using an alter table statement:

```sql
ALTER TABLE sales REORGANIZE PARTITION ASIA INTO
     (PARTITION ASIA VALUES IN ('CHN','JPN','IND', 'KOR'));
```
```
Query OK, 0 rows affected (1.28 sec)
Records: 0  Duplicates: 0  Warnings: 0
```
That seemed to do the trick, but let’s check the metadata just to be sure:

```sql
SELECT partition_name, partition_expression,
      partition_description
    FROM information_schema.partitions
    WHERE table_name = 'sales'
    ORDER BY partition_ordinal_position;
```
```
+----------------+----------------------+---------------------------------+
| PARTITION_NAME | PARTITION_EXPRESSION | PARTITION_DESCRIPTION           |
+----------------+----------------------+---------------------------------+
| NORTHAMERICA   | `geo_region_cd`      | 'US_NE','US_SE','US_MW','US_NW',|
|                |                      | 'US_SW','CAN','MEX'             |
| EUROPE         | `geo_region_cd`      | 'EUR_E','EUR_W'                 |
| ASIA           | `geo_region_cd`      | 'CHN','JPN','IND','KOR'         |
+----------------+----------------------+---------------------------------+
3 rows in set (0.00 sec)
```
Korea has indeed been added to the asia partition, and the data insertion will now proceed without any issues:

```sql
INSERT INTO sales
    VALUES
     (1, 1, 1, '2020-01-18', 'US_NE', 2765.15),
     (2, 3, 4, '2020-02-07', 'CAN', 5322.08),
     (3, 6, 27, '2020-03-11', 'KOR', 4267.12);
```
```
Query OK, 3 rows affected (0.26 sec)
Records: 3  Duplicates: 0  Warnings: 0
```
범위 분할을 사용하면 최대값 분할이 다른 분할에 매핑되지 않는 모든 행을 잡을 수 있지만 목록 분할은 스필오버 분할을 제공하지 않는다는 점을 명심하는 것이 중요합니다. 따라서 다른 열 값을 추가해야 할 때마다(예: 회사가 호주에서 제품 판매를 시작함) 새 값이 있는 행을 테이블에 추가하기 전에 분할 정의를 수정해야 합니다.

### Hash partitioning

파티션 키 열이 범위 또는 목록 파티셔닝에 적합하지 않은 경우 파티션 세트에 행을 균등하게 분배하기 위해 노력하는 세 번째 옵션이 있습니다. 서버는 열 값에 해싱 기능을 적용하여 이를 수행하며 이러한 유형의 분할을 (당연히) 해시 분할이라고 합니다. 분할 키로 선택한 열에 적은 수의 값만 포함되어야 하는 목록 분할과 달리 해시 분할은 분할 키 열에 많은 수의 고유 값이 포함될 때 가장 잘 작동합니다. 다음은 판매 테이블의 다른 버전이지만 cust_id 열의 값을 해시하여 생성된 4개의 해시 파티션이 있습니다.

```sql
CREATE TABLE sales
     (sale_id INT NOT NULL,
      cust_id INT NOT NULL,
      store_id INT NOT NULL,
      sale_date DATE NOT NULL,
      amount DECIMAL(9,2)
     )
    PARTITION BY HASH (cust_id)
      PARTITIONS 4
       (PARTITION H1,
        PARTITION H2,
        PARTITION H3,
        PARTITION H4
       );
```
```
Query OK, 0 rows affected (1.50 sec)
```
When rows are added to the sales table, they will be evenly distributed across the four partitions, which I named H1, H2, H3, and H4. In order to see how good a job it does, let’s add 16 rows, each with a different value for the cust_id column:

```sql
INSERT INTO sales
    VALUES
     (1, 1, 1, '2020-01-18', 1.1), (2, 3, 4, '2020-02-07', 1.2),
     (3, 17, 5, '2020-01-19', 1.3), (4, 23, 2, '2020-02-08', 1.4),
     (5, 56, 1, '2020-01-20', 1.6), (6, 77, 5, '2020-02-09', 1.7),
     (7, 122, 4, '2020-01-21', 1.8), (8, 153, 1, '2020-02-10', 1.9),
     (9, 179, 5, '2020-01-22', 2.0), (10, 244, 2, '2020-02-11', 2.1),
     (11, 263, 1, '2020-01-23', 2.2), (12, 312, 4, '2020-02-12', 2.3),
     (13, 346, 2, '2020-01-24', 2.4), (14, 389, 3, '2020-02-13', 2.5),
     (15, 472, 1, '2020-01-25', 2.6), (16, 502, 1, '2020-02-14', 2.7);
```
```
Query OK, 16 rows affected (0.19 sec)
Records: 16  Duplicates: 0  Warnings: 0
```
If the hashing function does a good job of distributing the rows evenly, we should ideally see four rows in each of the four partitions:

```sql
SELECT concat('# of rows in H1 = ', count(*)) partition_rowcount
    FROM sales PARTITION (h1) UNION ALL
    SELECT concat('# of rows in H2 = ', count(*)) partition_rowcount
    FROM sales PARTITION (h2) UNION ALL
    SELECT concat('# of rows in H3 = ', count(*)) partition_rowcount
    FROM sales PARTITION (h3) UNION ALL
    SELECT concat('# of rows in H4 = ', count(*)) partition_rowcount
    FROM sales PARTITION (h4);
```
```
+---------------------+
| partition_rowcount  |
+---------------------+
| # of rows in H1 = 4 |
| # of rows in H2 = 5 |
| # of rows in H3 = 3 |
| # of rows in H4 = 4 |
+---------------------+
4 rows in set (0.00 sec)
```
16개 행만 삽입되었다는 점을 감안할 때 이는 꽤 좋은 분포이며 행 수가 증가함에 따라 cust_id 열에 대해 상당히 많은 수의 고유 값이 있는 한 각 파티션에는 행의 거의 25%가 포함되어야 합니다.

### 복합 파티셔닝

데이터가 파티션에 할당되는 방식을 보다 세밀하게 제어해야 하는 경우 동일한 테이블에 대해 두 가지 다른 유형의 파티션을 사용할 수 있는 복합 파티셔닝을 사용할 수 있습니다. 복합 파티셔닝에서 첫 번째 파티셔닝 방법은 파티션을 정의하고 두 번째 파티셔닝 방법은 하위 파티션을 정의합니다. 다음은 범위 및 해시 분할을 모두 사용하여 판매 테이블을 다시 사용하는 예입니다.

```sql
CREATE TABLE sales
     (sale_id INT NOT NULL,
      cust_id INT NOT NULL,
      store_id INT NOT NULL,
      sale_date DATE NOT NULL,
      amount DECIMAL(9,2)
     )
    PARTITION BY RANGE (yearweek(sale_date))
    SUBPARTITION BY HASH (cust_id)
     (PARTITION s1 VALUES LESS THAN (202002)
        (SUBPARTITION s1_h1,
         SUBPARTITION s1_h2,
         SUBPARTITION s1_h3,
         SUBPARTITION s1_h4),
      PARTITION s2 VALUES LESS THAN (202003)
        (SUBPARTITION s2_h1,
         SUBPARTITION s2_h2,
         SUBPARTITION s2_h3,
         SUBPARTITION s2_h4),
      PARTITION s3 VALUES LESS THAN (202004)
        (SUBPARTITION s3_h1,
         SUBPARTITION s3_h2,
         SUBPARTITION s3_h3,
         SUBPARTITION s3_h4),
      PARTITION s4 VALUES LESS THAN (202005)
        (SUBPARTITION s4_h1,
         SUBPARTITION s4_h2,
         SUBPARTITION s4_h3,
         SUBPARTITION s4_h4),
      PARTITION s5 VALUES LESS THAN (202006)
        (SUBPARTITION s5_h1,
         SUBPARTITION s5_h2,
         SUBPARTITION s5_h3,
         SUBPARTITION s5_h4),
      PARTITION s999 VALUES LESS THAN (MAXVALUE)
        (SUBPARTITION s999_h1,
         SUBPARTITION s999_h2,
         SUBPARTITION s999_h3,
         SUBPARTITION s999_h4)
     );
```
```
Query OK, 0 rows affected (9.72 sec)
```
There are 6 partitions, each having 4 subpartitions, for a total of 24 subpartitions. Next, let’s reinsert the 16 rows from the earlier example for hash partitioning:

```sql
INSERT INTO sales
    VALUES
     (1, 1, 1, '2020-01-18', 1.1), (2, 3, 4, '2020-02-07', 1.2),
     (3, 17, 5, '2020-01-19', 1.3), (4, 23, 2, '2020-02-08', 1.4),
     (5, 56, 1, '2020-01-20', 1.6), (6, 77, 5, '2020-02-09', 1.7),
     (7, 122, 4, '2020-01-21', 1.8), (8, 153, 1, '2020-02-10', 1.9),
     (9, 179, 5, '2020-01-22', 2.0), (10, 244, 2, '2020-02-11', 2.1),
     (11, 263, 1, '2020-01-23', 2.2), (12, 312, 4, '2020-02-12', 2.3),
     (13, 346, 2, '2020-01-24', 2.4), (14, 389, 3, '2020-02-13', 2.5),
     (15, 472, 1, '2020-01-25', 2.6), (16, 502, 1, '2020-02-14', 2.7);
```
```
Query OK, 16 rows affected (0.22 sec)
```
Records: 16  Duplicates: 0  Warnings: 0
When you query the sales table, you can retrieve data from one of the partitions, in which case you retrieve data from the four subpartitions associated with the partition:

```sql
SELECT *
    FROM sales PARTITION (s3);
```
```
+---------+---------+----------+------------+--------+
| sale_id | cust_id | store_id | sale_date  | amount |
+---------+---------+----------+------------+--------+
|       5 |      56 |        1 | 2020-01-20 |   1.60 |
|      15 |     472 |        1 | 2020-01-25 |   2.60 |
|       3 |      17 |        5 | 2020-01-19 |   1.30 |
|       7 |     122 |        4 | 2020-01-21 |   1.80 |
|      13 |     346 |        2 | 2020-01-24 |   2.40 |
|       9 |     179 |        5 | 2020-01-22 |   2.00 |
|      11 |     263 |        1 | 2020-01-23 |   2.20 |
+---------+---------+----------+------------+--------+
7 rows in set (0.00 sec)
```
Because the table is subpartitioned, you may also retrieve data from a single subpartition:

```sql
SELECT *
    FROM sales PARTITION (s3_h3);
```
```
+---------+---------+----------+------------+--------+
| sale_id | cust_id | store_id | sale_date  | amount |
+---------+---------+----------+------------+--------+
|       7 |     122 |        4 | 2020-01-21 |   1.80 |
|      13 |     346 |        2 | 2020-01-24 |   2.40 |
+---------+---------+----------+------------+--------+
2 rows in set (0.00 sec)
```
This query retrieves data only from the s3_h3 subpartition of the s3 partition.

### Partitioning Benefits
파티셔닝의 한 가지 주요 이점은 전체 테이블이 아닌 하나의 파티션과만 상호 작용하면 된다는 것입니다. 예를 들어, 테이블이 sales_date 열에서 범위로 분할되고 WHERE sales_date BETWEEN '2019-12-01' AND '2020-01-15'와 같은 필터 조건이 포함된 쿼리를 실행하면 서버에서 확인합니다. 테이블의 메타데이터를 사용하여 실제로 포함해야 하는 파티션을 결정합니다. 이 개념을 파티션 프루닝이라고 하며 테이블 파티셔닝의 가장 큰 장점 중 하나입니다.

마찬가지로 분할된 테이블에 대한 조인을 포함하는 쿼리를 실행하고 쿼리가 분할 열에 대한 조건을 포함하는 경우 서버는 쿼리와 관련된 데이터를 포함하지 않는 모든 파티션을 제외할 수 있습니다. 이를 파티션 방식 조인이라고 하며 쿼리에 필요한 데이터가 포함된 파티션만 포함된다는 점에서 파티션 정리와 유사합니다.

관리적 관점에서 파티셔닝의 주요 이점 중 하나는 더 이상 필요하지 않은 데이터를 빠르게 삭제할 수 있다는 것입니다. 예를 들어, 재무 데이터는 7년 동안 온라인 상태로 유지해야 합니다. 테이블이 트랜잭션 날짜를 기반으로 분할된 경우 7년보다 오래된 데이터를 보유하는 모든 파티션은 삭제될 수 있습니다. 파티션을 나눈 테이블의 또 다른 관리 이점은 여러 파티션에서 동시에 업데이트를 수행할 수 있다는 점입니다. 이는 테이블의 모든 행을 터치하는 데 필요한 시간을 크게 줄일 수 있다는 것입니다.

### Clustering

합리적인 분할 전략과 결합된 충분한 스토리지를 사용하면 단일 관계형 데이터베이스에 많은 양의 데이터를 저장할 수 있습니다. 그러나 수천 명의 동시 사용자를 처리해야 하거나 야간 주기 동안 수만 개의 보고서를 생성해야 하는 경우에는 어떻게 될까요? 데이터 저장소가 충분하더라도 단일 서버 내에서 CPU, 메모리 또는 네트워크 대역폭이 충분하지 않을 수 있습니다. 한 가지 잠재적인 대답은 클러스터링으로, 여러 서버가 단일 데이터베이스처럼 작동할 수 있습니다.

여러 클러스터링 아키텍처가 있지만 이 논의의 목적을 위해 클러스터의 모든 서버가 모든 디스크에 액세스할 수 있고 한 서버에 캐시된 데이터는 모든 사용자가 액세스할 수 있는 공유 디스크/공유 캐시 구성을 참조합니다. 클러스터의 다른 서버. 이러한 유형의 아키텍처를 사용하면 응용 프로그램 서버가 클러스터의 데이터베이스 서버 중 하나에 연결할 수 있으며 연결이 실패할 경우 클러스터의 다른 서버로 자동으로 장애 조치됩니다. 8개 서버 클러스터를 사용하면 매우 많은 수의 동시 사용자 및 관련 쿼리/보고서/작업을 처리할 수 있어야 합니다.

상용 데이터베이스 공급업체 중 Oracle은 이 분야의 선두 주자입니다. 많은 세계 최대 기업에서 Oracle Exadata 플랫폼을 사용하여 수천 명의 동시 사용자가 액세스하는 초대형 데이터베이스를 호스팅하고 있습니다. 그러나 이 플랫폼조차도 Google, Facebook, Amazon 및 기타 회사가 새로운 길을 개척하게 만든 가장 큰 회사의 요구를 충족하지 못합니다.

### Sharding

새로운 소셜 미디어 회사의 데이터 설계자로 고용되었다고 가정해 보겠습니다. 약 10억 명의 사용자가 있을 것으로 예상되며 각 사용자는 평균적으로 하루에 3.7개의 메시지를 생성하며 데이터는 무기한으로 사용할 수 있어야 합니다. 몇 가지 계산을 수행한 후 사용 가능한 가장 큰 관계형 데이터베이스 플랫폼을 1년 이내에 소진할 것이라고 결정했습니다. 한 가지 가능성은 개별 테이블뿐만 아니라 전체 데이터베이스를 분할하는 것입니다. 샤딩으로 알려진 이 접근 방식은 데이터를 여러 데이터베이스(샤드라고 함)에 분할하므로 테이블 분할과 유사하지만 규모가 더 크고 훨씬 더 복잡합니다. 소셜 미디어 회사에 이 전략을 적용하려는 경우 각각 약 천만 명의 사용자에 대한 데이터를 호스팅하는 100개의 개별 데이터베이스를 구현하기로 결정할 수 있습니다.

샤딩은 복잡한 주제이며 이 책은 입문서이므로 자세히 설명하지는 않겠지만 해결해야 할 몇 가지 문제는 다음과 같습니다.

- 연결할 데이터베이스를 결정하는 데 사용되는 값인 샤딩 키를 선택해야 합니다.
큰 테이블은 조각으로 나뉘고 개별 행이 단일 샤드에 할당되지만 작은 참조 테이블은 모든 샤드에 복제해야 할 수 있으며 참조 데이터를 수정하고 변경 사항을 모든 샤드에 전파하는 방법에 대한 전략을 정의해야 합니다. .

- 개별 샤드가 너무 커지면(예: 소셜 미디어 회사의 사용자가 현재 20억 명임) 더 많은 샤드를 추가하고 샤드 전체에 데이터를 재분배하기 위한 계획이 필요합니다.
스키마를 변경해야 하는 경우 모든 스키마가 동기화 상태를 유지하도록 모든 샤드에 변경 사항을 배포하는 전략이 필요합니다.

- 애플리케이션 로직이 둘 이상의 샤드에 저장된 데이터에 액세스해야 하는 경우 여러 데이터베이스에서 쿼리하는 방법과 여러 데이터베이스에서 트랜잭션을 구현하는 방법에 대한 전략이 필요합니다.

이것이 복잡해 보인다면 2000년대 후반에 많은 기업이 새로운 접근 방식을 찾기 시작했기 때문입니다. 다음 섹션에서는 관계형 데이터베이스 영역을 완전히 벗어난 매우 큰 데이터 세트를 처리하기 위한 다른 전략을 살펴봅니다.

## Big Data

샤딩의 장단점을 평가하는 데 시간을 투자한 후 귀하(소셜 미디어 회사의 데이터 설계자)가 다른 접근 방식을 조사하기로 결정했다고 가정해 보겠습니다. 자신만의 길을 개척하려고 하기보다 방대한 양의 데이터를 처리하는 다른 회사(Amazon, Google, Facebook, Twitter와 같은 회사)에서 수행한 작업을 검토하는 것이 좋습니다. 이들 회사(및 기타 회사)가 개척한 일련의 기술은 빅 데이터로 브랜드화되어 업계 유행어가 되었지만 몇 가지 가능한 정의가 있습니다. 빅 데이터의 경계를 정의하는 한 가지 방법은 "3 Vs"를 사용하는 것입니다:

**Volume**
In this context, volume generally means billions or trillions of data points.

**Velocity**
This is a measure of how quickly data arrives.

**Variety**
This means that data is not always structured (as in rows and columns in a relational database) but can also be unstructured (e.g., emails, videos, photos, audio files, etc.).

따라서 빅 데이터를 특성화하는 한 가지 방법은 빠른 속도로 도달하는 다양한 형식의 방대한 양의 데이터를 처리하도록 설계된 시스템입니다. 다음 섹션에서는 지난 15년 동안 진화한 일부 빅 데이터 기술에 대해 간략하게 설명합니다.

### Hadoop
Hadoop is best described as an ecosystem, or a set of technologies and tools that work together. Some of the major components of Hadoop include:

### Hadoop Distributed File System (HDFS)
Like the name implies, HDFS enables file management across a large number of servers.

### MapReduce
This technology processes large amounts of structured and unstructured data by breaking a task into many small pieces that can be run in parallel across many servers.

### YARN
This is a resource manager and job scheduler for HDFS.

이러한 기술을 함께 사용하면 단일 논리 시스템으로 작동하는 수백 또는 수천 대의 서버에서 파일을 저장하고 처리할 수 있습니다. Hadoop이 널리 사용되지만 MapReduce를 사용하여 데이터를 쿼리하려면 일반적으로 프로그래머가 필요하므로 Hive, Impala 및 Drill을 비롯한 여러 SQL 인터페이스가 개발되었습니다.

### NoSQL and Document Databases

관계형 데이터베이스에서 데이터는 일반적으로 숫자, 문자열, 날짜 등을 포함하는 열로 구성된 테이블로 구성된 미리 정의된 스키마를 따라야 합니다. 그러나 데이터의 구조를 미리 알지 못하거나 구조가 알려져 있지만 자주 변경됩니까? 많은 기업의 대답은 데이터와 스키마 정의를 XML 또는 JSON과 같은 형식을 사용하여 문서로 결합한 다음 데이터베이스에 문서를 저장하는 것입니다. 이렇게 하면 스키마를 수정할 필요 없이 다양한 유형의 데이터를 동일한 데이터베이스에 저장할 수 있으므로 저장이 더 쉬워지지만 문서에 저장된 데이터를 이해하기 위한 쿼리 및 분석 도구에 부담이 됩니다.

문서 데이터베이스는 일반적으로 간단한 키-값 메커니즘을 사용하여 데이터를 저장하는 NoSQL 데이터베이스의 하위 집합입니다. 예를 들어 MongoDB와 같은 문서 데이터베이스를 사용하면 고객 ID를 키로 사용하여 고객의 모든 데이터가 포함된 JSON 문서를 저장할 수 있고 다른 사용자는 문서에 저장된 스키마를 읽어 저장된 데이터를 이해할 수 있습니다.

### Cloud Computing
빅 데이터가 도래하기 전에 대부분의 기업은 기업 전체에서 사용되는 데이터베이스, 웹 및 애플리케이션 서버를 수용하기 위해 자체 데이터 센터를 구축해야 했습니다. 클라우드 컴퓨팅의 출현으로 데이터 센터를 AWS, Microsoft Azure 또는 Google Cloud와 같은 플랫폼에 기본적으로 아웃소싱하도록 선택할 수 있습니다. 클라우드에서 서비스를 호스팅할 때의 가장 큰 이점 중 하나는 즉각적인 확장성으로 서비스를 실행하는 데 필요한 컴퓨팅 성능의 양을 빠르게 늘리거나 줄일 수 있습니다. 신생 기업은 서버, 스토리지, 네트워크 또는 소프트웨어 라이선스에 대한 초기 비용 없이 코드 작성을 시작할 수 있기 때문에 이러한 플랫폼을 좋아합니다.

데이터베이스와 관련하여 AWS의 데이터베이스 및 분석 제품을 간단히 살펴보면 다음과 같은 옵션이 있습니다.

- Relational databases (MySQL, Aurora, PostgreSQL, MariaDB, Oracle, and SQL Server)
- In-memory database (ElastiCache)
- Data warehousing database (Redshift)
- NoSQL database (DynamoDB)
- Document database (DocumentDB)
- Graph database (Neptune)
- Time-series database (TimeStream)
- Hadoop (EMR)
- Data lakes (Lake Formation)

While relational databases dominated the landscape up until the mid-2000s, it’s pretty easy to see that companies are now mixing and matching various platforms and that relational databases may become less popular over time.

## Conclusion

데이터베이스는 점점 커지고 있지만 동시에 스토리지, 클러스터링 및 파티셔닝 기술은 더욱 강력해지고 있습니다. 기술 스택에 관계없이 방대한 양의 데이터로 작업하는 것은 상당히 어려울 수 있습니다. 관계형 데이터베이스, 빅 데이터 플랫폼 또는 다양한 데이터베이스 서버를 사용하든 SQL은 다양한 기술에서 데이터 검색을 용이하게 하기 위해 진화하고 있습니다. 이것은 이 책의 마지막 장의 주제가 될 것입니다. 여기서는 SQL 엔진을 사용하여 여러 형식으로 저장된 데이터를 쿼리하는 방법을 보여줍니다.