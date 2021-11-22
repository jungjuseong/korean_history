# Chapter 13. Indexes and Constraints

이 책의 초점은 프로그래밍 기술이기 때문에 처음 12개 장은 강력한 선택, 삽입, 업데이트 및 삭제 문을 만드는 데 사용할 수 있는 SQL 언어의 요소에 집중했습니다. 그러나 다른 데이터베이스 기능은 작성하는 코드에 간접적으로 영향을 줍니다. 이 장에서는 인덱스와 제약 조건의 두 가지 기능에 중점을 둡니다.

## 인덱스

테이블에 행을 삽입할 때 데이터베이스 서버는 테이블 내의 특정 위치에 데이터를 넣지 않습니다. 예를 들어 고객 테이블에 행을 추가하면 서버는 customer_id 열을 통해 숫자 순서로 행을 배치하거나 last_name 열을 통해 알파벳 순서로 행을 배치하지 않습니다. 대신 서버는 파일 내의 다음 사용 가능한 위치에 데이터를 배치합니다(서버는 각 테이블에 대한 여유 공간 목록을 유지합니다). 따라서 고객 테이블을 쿼리할 때 서버는 쿼리에 응답하기 위해 테이블의 모든 행을 검사해야 합니다. 예를 들어 다음 쿼리를 실행한다고 가정해 보겠습니다.

```SQL
이름, 성 선택
     고객으로부터
     WHERE last_name LIKE 'Y%';
```
```
+------------+--------------+
| 이름 | 성 |
+------------+--------------+
| 루이스 | 야네즈 |
| 마빈 | 이 |
| 신시아 | 영 |
+------------+--------------+
3줄 세트(0.09초)
```
성이 Y로 시작하는 모든 고객을 찾으려면 서버는 고객 테이블의 각 행을 방문하여 last_name 열의 내용을 검사해야 합니다. 성이 Y로 시작하면 행이 결과 집합에 추가됩니다. 이러한 유형의 액세스를 테이블 스캔이라고 합니다.

이 방법은 행이 3개뿐인 테이블에 대해 잘 작동하지만 테이블에 300만 행이 포함된 경우 쿼리에 응답하는 데 얼마나 걸릴지 상상해 보십시오. 3보다 크고 300만보다 작은 일부 행 수에서 서버가 추가 도움 없이 합리적인 시간 내에 쿼리에 응답할 수 없는 선이 교차됩니다. 이 도움말은 고객 테이블에서 하나 이상의 인덱스 형태로 제공됩니다.

데이터베이스 색인에 대해 들어본 적이 없더라도 색인이 무엇인지 확실히 알고 있습니다(예: 이 책에 색인이 있습니다). 인덱스는 단순히 리소스 내에서 특정 항목을 찾기 위한 메커니즘입니다. 예를 들어, 각 기술 출판물에는 출판물 내에서 특정 단어나 구를 찾을 수 있는 색인이 끝에 있습니다. 색인은 이러한 단어와 구를 알파벳 순서로 나열하여 독자가 색인 내의 특정 문자로 빠르게 이동하고 원하는 항목을 찾은 다음 해당 단어나 구를 찾을 수 있는 페이지를 찾을 수 있도록 합니다.

사람이 색인을 사용하여 발행물 내에서 단어를 찾는 것과 같은 방식으로 데이터베이스 서버는 색인을 사용하여 테이블에서 행을 찾습니다. 인덱스는 일반 데이터 테이블과 달리 특정 순서로 유지되는 특수 테이블입니다. 그러나 항목에 대한 모든 데이터를 포함하는 대신 인덱스에는 데이터 테이블에서 행을 찾는 데 사용되는 열만 포함되며 행이 실제로 있는 위치를 설명하는 정보도 포함됩니다. 따라서 인덱스의 역할은 테이블의 모든 행을 검사할 필요 없이 테이블의 행과 열의 하위 집합 검색을 용이하게 하는 것입니다.

## 인덱스 생성

고객 테이블로 돌아가서 전자 메일 열에 인덱스를 추가하여 이 열에 대한 값을 지정하는 쿼리와 고객의 전자 메일 주소를 지정하는 업데이트 또는 삭제 작업의 속도를 높일 수 있습니다. MySQL 데이터베이스에 이러한 인덱스를 추가하는 방법은 다음과 같습니다.

```sql
ALTER TABLE customer
     ADD INDEX idx_email (email);
```
```
Query OK, 0 rows affected (1.87 sec)
Records: 0  Duplicates: 0  Warnings: 0
```
이 명령문은 customer.email 열에 인덱스(정확하게는 B-트리 인덱스이지만 더 자세히 설명함)를 생성합니다. 또한 인덱스에는 idx_email이라는 이름이 지정됩니다. 인덱스가 있는 경우 쿼리 최적화 프로그램(3장에서 설명)은 인덱스를 사용하는 것이 유익하다고 판단되는 경우 인덱스를 사용하도록 선택할 수 있습니다. 테이블에 둘 이상의 인덱스가 있는 경우 옵티마이저는 특정 SQL 문에 가장 유리한 인덱스를 결정해야 합니다.

---
**노트**

MySQL은 인덱스를 테이블의 선택적 구성 요소로 취급하므로 이전 버전에서는 alter table 명령을 사용하여 인덱스를 추가하거나 제거했습니다. SQL Server 및 Oracle Database를 비롯한 다른 데이터베이스 서버는 인덱스를 독립적인 스키마 개체로 취급합니다. 따라서 SQL Server와 Oracle 모두에 대해 다음과 같이 인덱스 생성 명령을 사용하여 인덱스를 생성합니다.

---

```sql
CREATE INDEX idx_email
ON customer (email);
```

As of MySQL version 5, a create index command is available, although it is mapped to the alter table command. You must still use the alter table command to create primary key indexes, however.

All database servers allow you to look at the available indexes. MySQL users can use the show command to see all of the indexes on a specific table, as in:

```sql
SHOW INDEX FROM customer \G;
```
```
*************************** 1. row ***************************
        Table: customer
   Non_unique: 0
     Key_name: PRIMARY
 Seq_in_index: 1
  Column_name: customer_id
    Collation: A
  Cardinality: 599
     Sub_part: NULL
       Packed: NULL
         Null:
   Index_type: BTREE
...
*************************** 2. row ***************************
        Table: customer
   Non_unique: 1
     Key_name: idx_fk_store_id
 Seq_in_index: 1
  Column_name: store_id
    Collation: A
  Cardinality: 2
     Sub_part: NULL
       Packed: NULL
         Null:
   Index_type: BTREE
...
*************************** 3. row ***************************
        Table: customer
   Non_unique: 1
     Key_name: idx_fk_address_id
 Seq_in_index: 1
  Column_name: address_id
    Collation: A
  Cardinality: 599
     Sub_part: NULL
       Packed: NULL
         Null:
   Index_type: BTREE
...
*************************** 4. row ***************************
        Table: customer
   Non_unique: 1
     Key_name: idx_last_name
 Seq_in_index: 1
  Column_name: last_name
    Collation: A
  Cardinality: 599
     Sub_part: NULL
       Packed: NULL
         Null:
   Index_type: BTREE
...
*************************** 5. row ***************************
        Table: customer
   Non_unique: 1
     Key_name: idx_email
 Seq_in_index: 1
  Column_name: email
    Collation: A
  Cardinality: 599
     Sub_part: NULL
       Packed: NULL
         Null: YES
   Index_type: BTREE
...
5 rows in set (0.06 sec)
```
출력은 고객 테이블에 5개의 인덱스가 있음을 보여줍니다. 하나는 PRIMARY라는 customer_id 열에 있고 나머지 4개는 store_id, address_id, last_name 및 email 열에 있습니다. 이 인덱스의 출처가 궁금하시다면 이메일 열에 인덱스를 만들고 나머지는 샘플 Sakila 데이터베이스의 일부로 설치했습니다. 다음은 테이블을 만드는 데 사용된 명령문입니다.

```sql
CREATE TABLE customer (
  customer_id SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
  store_id TINYINT UNSIGNED NOT NULL,
  first_name VARCHAR(45) NOT NULL,
  last_name VARCHAR(45) NOT NULL,
  email VARCHAR(50) DEFAULT NULL,
  address_id SMALLINT UNSIGNED NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  create_date DATETIME NOT NULL,
  last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY  (customer_id),
  KEY idx_fk_store_id (store_id),
  KEY idx_fk_address_id (address_id),
  KEY idx_last_name (last_name),
  ...
```

테이블이 생성될 때 MySQL 서버는 기본 키 열(이 경우 customer_id)에 인덱스를 자동으로 생성하고 인덱스 이름을 PRIMARY로 지정했습니다. 이것은 기본 키 제약 조건과 함께 사용되는 특별한 유형의 인덱스이지만 이 장의 뒷부분에서 제약 조건을 다룰 것입니다.

인덱스를 생성한 후 인덱스가 유용하지 않다고 판단되면 다음을 통해 제거할 수 있습니다.

```sql
ALTER TABLE customer
     DROP INDEX idx_email;
```
```
Query OK, 0 rows affected (0.50 sec)
Records: 0  Duplicates: 0  Warnings: 0
```
---
NOTE

SQL Server and Oracle Database users must use the drop index command to remove an index, as in:
```sql
DROP INDEX idx_email; (Oracle)

DROP INDEX idx_email ON customer; (SQL Server)
```

MySQL now also supports the drop index command, although it is also mapped to the alter table command.

---

### Unique indexes

데이터베이스를 설계할 때 중복 데이터를 포함할 수 있는 열과 포함할 수 없는 열을 고려하는 것이 중요합니다. 예를 들어, 각 행에 서로 다른 식별자(customer_id), 이메일 및 주소가 있으므로 고객 테이블에 John Smith라는 두 명의 고객이 있을 수 있습니다. 그러나 두 명의 다른 고객이 동일한 이메일 주소를 사용하도록 허용하고 싶지는 않습니다. customer.email 열에 고유 인덱스를 만들어 중복 값에 대한 규칙을 적용할 수 있습니다.

고유 인덱스는 여러 역할을 합니다. 일반 인덱스의 모든 이점을 제공할 뿐만 아니라 인덱스된 열에서 중복 값을 허용하지 않는 메커니즘으로도 사용됩니다. 행이 삽입되거나 인덱싱된 열이 수정될 때마다 데이터베이스 서버는 고유 인덱스를 확인하여 해당 값이 테이블의 다른 행에 이미 존재하는지 확인합니다.

customer.email 열에 고유 인덱스를 만드는 방법은 다음과 같습니다.

```sql
ALTER TABLE customer
     ADD UNIQUE idx_email (email);
```
```
Query OK, 0 rows affected (0.64 sec)
Records: 0  Duplicates: 0  Warnings: 0
```

---
NOTE

SQL Server and Oracle Database users need only add the unique keyword when creating an index, as in:

```sql
CREATE UNIQUE INDEX idx_email
ON customer (email);
```

---

With the index in place, you will receive an error if you try to add a new customer with an email address that already exists:

```sql
INSERT INTO customer
      (store_id, first_name, last_name, email, address_id, active)
     VALUES
      (1,'ALAN','KAHN', 'ALAN.KAHN@sakilacustomer.org', 394, 1);
```
```
ERROR 1062 (23000): Duplicate entry 'ALAN.KAHN@sakilacustomer.org' 
  for key 'idx_email'
```

서버가 이미 기본 키 값의 고유성을 확인하기 때문에 기본 키 열에 고유 인덱스를 구축하면 안 됩니다. 그러나 필요하다고 생각되는 경우 동일한 테이블에 둘 이상의 고유 인덱스를 생성할 수 있습니다.

## 다중 열 인덱스

지금까지 설명한 단일 열 인덱스와 함께 여러 열에 걸쳐 있는 인덱스를 작성할 수도 있습니다. 예를 들어 이름과 성을 기준으로 고객을 검색하는 경우 다음과 같이 두 열에 인덱스를 함께 작성할 수 있습니다.

```sql
ALTER TABLE customer
     ADD INDEX idx_full_name (last_name, first_name);
```
```
Query OK, 0 rows affected (0.35 sec)
Records: 0  Duplicates: 0  Warnings: 0
```
이 인덱스는 이름과 성 또는 성을 지정하는 쿼리에는 유용하지만 고객의 이름만 지정하는 쿼리에는 유용하지 않습니다. 그 이유를 이해하려면 상대방의 전화번호를 찾는 방법을 고려하십시오. 그 사람의 이름과 성을 알고 있다면 전화번호부가 성, 이름 순으로 구성되어 있으므로 전화번호부를 사용하여 빠르게 번호를 찾을 수 있습니다. 그 사람의 이름만 알고 있다면 전화번호부의 모든 항목을 스캔하여 지정된 이름을 가진 모든 항목을 찾아야 합니다.

따라서 다중 열 인덱스를 작성할 때 인덱스를 최대한 유용하게 만들려면 먼저 나열할 열, 두 번째로 나열할 열 등을 신중하게 고려해야 합니다. 그러나 적절한 응답 시간을 보장하기 위해 필요하다고 생각되는 경우 동일한 열 집합을 사용하여 다른 순서로 여러 인덱스를 빌드하는 것을 막을 수 있는 방법은 없습니다.


## 인덱스 유형

인덱싱은 강력한 도구이지만 다양한 유형의 데이터가 있기 때문에 단일 인덱싱 전략이 항상 작동하는 것은 아닙니다. 다음 섹션에서는 다양한 서버에서 사용할 수 있는 다양한 유형의 인덱싱을 보여줍니다.

### B-tree indexes

지금까지 표시된 모든 인덱스는 B-트리 인덱스로 더 일반적으로 알려진 균형 트리 인덱스입니다. MySQL, Oracle Database 및 SQL Server는 모두 기본적으로 B-트리 인덱싱을 사용하므로 다른 유형을 명시적으로 요청하지 않는 한 B-트리 인덱스를 얻습니다. 예상할 수 있듯이 B-트리 인덱스는 단일 수준의 리프 노드로 이어지는 하나 이상의 분기 노드 수준이 있는 트리로 구성됩니다. 분기 노드는 트리 탐색에 사용되는 반면 리프 노드는 실제 값과 위치 정보를 보유합니다. 예를 들어 customer.last_name 열에 구축된 B-트리 인덱스는 그림 13-1과 비슷할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781492057604/files/assets/lsq3_1301.png)

그림 13-1. B-트리 예제

성이 G로 시작하는 모든 고객을 검색하기 위해 쿼리를 발행하는 경우 서버는 최상위 분기 노드(루트 노드라고 함)를 보고 A에서 M으로 시작하는 성을 처리하는 분기 노드에 대한 링크를 따릅니다. 이 분기 노드는 차례로 서버를 G부터 I까지의 성을 포함하는 리프 노드로 지시합니다. 그런 다음 서버는 G로 시작하지 않는 값을 만날 때까지 리프 노드의 값을 읽기 시작합니다(이 , 이 경우 호손).

고객 테이블에서 행이 삽입, 업데이트 및 삭제될 때 서버는 루트 노드의 한쪽에 다른 쪽보다 훨씬 더 많은 분기/리프 노드가 없도록 트리 균형을 유지하려고 시도합니다. 서버는 값을 보다 균등하게 재분배하기 위해 분기 노드를 추가하거나 제거할 수 있으며 전체 수준의 분기 노드를 추가하거나 제거할 수도 있습니다. 트리 균형을 유지함으로써 서버는 여러 수준의 분기 노드를 탐색하지 않고도 원하는 값을 찾기 위해 리프 노드로 빠르게 이동할 수 있습니다.

### 비트맵 인덱스

B-트리 인덱스는 고객의 이름/성과 같이 다양한 값을 포함하는 열을 처리하는 데 탁월하지만 소수의 값만 허용하는 열에 구축할 경우 다루기 어려워질 수 있습니다. 예를 들어 모든 활성 또는 비활성 계정을 빠르게 검색할 수 있도록 customer.active 열에 대한 인덱스를 생성하기로 결정할 수 있습니다. 그러나 두 가지 다른 값(활성의 경우 1, 비활성의 경우 0으로 저장)만 있고 활성 고객이 훨씬 더 많기 때문에 고객 수가 증가함에 따라 균형 잡힌 B-트리 인덱스를 유지하기 어려울 수 있습니다.

많은 수의 행(낮은 카디널리티 데이터라고 함)에서 소수의 값만 포함하는 열의 경우 다른 인덱싱 전략이 필요합니다. 이러한 상황을 보다 효율적으로 처리하기 위해 Oracle Database에는 열에 저장된 각 값에 대한 비트맵을 생성하는 비트맵 인덱스가 포함되어 있습니다. customer.active 열에 비트맵 인덱스를 작성하는 경우 인덱스는 두 개의 비트맵을 유지합니다. 하나는 값 0에 대한 것이고 다른 하나는 값 1에 대한 것입니다. 모든 비활성 고객을 검색하는 쿼리를 작성할 때 데이터베이스 서버는 0 비트맵을 선택하고 원하는 행을 빠르게 검색합니다.

비트맵 인덱스는 카디널리티가 낮은 데이터를 위한 훌륭하고 컴팩트한 인덱싱 솔루션이지만, 이 인덱싱 전략은 열에 저장된 값의 수가 행의 수(높은 카디널리티 데이터라고 함)와 관련하여 너무 높아지면 중단됩니다. 서버는 너무 많은 비트맵을 유지해야 합니다. 예를 들어, 기본 키 열에 비트맵 인덱스를 만들지 않을 것입니다. 왜냐하면 이것이 가능한 가장 높은 카디널리티(모든 행에 대해 다른 값)를 나타내기 때문입니다.

Oracle 사용자는 다음과 같이 create index 문에 bitmap 키워드를 추가하여 비트맵 인덱스를 생성할 수 있습니다.

```sql
CREATE BITMAP INDEX idx_active ON customer (active);
```

비트맵 인덱스는 일반적으로 상대적으로 적은 값(예: 판매 분기, 지리적 지역, 제품, 영업 사원)을 포함하는 열에 많은 양의 데이터가 인덱싱되는 데이터 웨어하우징 환경에서 사용됩니다.

### Text 인덱스

데이터베이스에 문서가 저장되어 있는 경우 사용자가 문서에서 단어나 구를 검색하도록 허용해야 할 수 있습니다. 서버가 검색이 요청될 때마다 각 문서를 정독하고 원하는 텍스트를 스캔하는 것을 확실히 원하지는 않지만 전통적인 인덱싱 전략은 이 상황에서 작동하지 않습니다. 이러한 상황을 처리하기 위해 MySQL, SQL Server 및 Oracle Database에는 문서에 대한 특수 인덱싱 및 검색 메커니즘이 포함되어 있습니다. SQL Server와 MySQL에는 모두 전체 텍스트 인덱스라고 하는 것이 포함되어 있으며 Oracle Database에는 Oracle Text로 알려진 강력한 도구 세트가 포함되어 있습니다. 문서 검색은 예를 보여주는 것이 실용적이지 않을 정도로 전문화되어 있지만 최소한 사용 가능한 것을 아는 것은 유용합니다.


## 인덱스 사용 방법

인덱스는 일반적으로 서버에서 특정 테이블의 행을 빠르게 찾는 데 사용되며, 그 후에 서버는 사용자가 요청한 추가 정보를 추출하기 위해 관련 테이블을 방문합니다. 다음 쿼리를 고려하십시오.

```sql
SELECT customer_id, first_name, last_name
     FROM customer
     WHERE first_name LIKE 'S%' AND last_name LIKE 'P%';
```
```
+-------------+------------+-----------+
| customer_id | first_name | last_name |
+-------------+------------+-----------+
|          84 | SARA       | PERRY     |
|         197 | SUE        | PETERS    |
|         167 | SALLY      | PIERCE    |
+-------------+------------+-----------+
3 rows in set (0.00 sec)
```
이 쿼리의 경우 서버는 다음 전략 중 하나를 사용할 수 있습니다.

- 고객 테이블의 모든 행을 스캔합니다.
- last_name 열의 인덱스를 사용하여 성이 P로 시작하는 모든 고객을 찾습니다. 그런 다음 고객 테이블의 각 행을 방문하여 이름이 S로 시작하는 행만 찾습니다.
- last_name 및 first_name 열의 인덱스를 사용하여 성이 P로 시작하고 이름이 S로 시작하는 모든 고객을 찾습니다.

인덱스가 테이블을 다시 방문할 필요 없이 결과 집합에 필요한 모든 행을 생성하기 때문에 세 번째 선택이 가장 좋은 옵션인 것 같습니다. 그러나 세 가지 옵션 중 어느 것이 활용될지 어떻게 알 수 있습니까? MySQL의 쿼리 최적화 프로그램이 쿼리를 실행하기로 결정하는 방법을 확인하려면 쿼리를 실행하는 대신 서버에 쿼리 실행 계획을 표시하도록 Explain 문을 사용합니다.

```sql
EXPLAIN
     SELECT customer_id, first_name, last_name
     FROM customer
     WHERE first_name LIKE 'S%' AND last_name LIKE 'P%' \G;
```
```
*************************** 1. row ***************************
           id: 1
  select_type: SIMPLE
        table: customer
   partitions: NULL
         type: range
possible_keys: idx_last_name,idx_full_name
          key: idx_full_name
      key_len: 274
          ref: NULL
         rows: 28
     filtered: 11.11
        Extra: Using where; Using index
1 row in set, 1 warning (0.00 sec)
```
---
노트

각 데이터베이스 서버에는 쿼리 최적화 프로그램이 SQL 문을 처리하는 방법을 볼 수 있는 도구가 포함되어 있습니다. SQL Server를 사용하면 SQL 문을 실행하기 전에 showplan_text 문 집합을 실행하여 실행 계획을 볼 수 있습니다. Oracle Database에는 plan_table이라는 특수 테이블에 실행 계획을 기록하는 Explain Plan 문이 포함되어 있습니다.

---

쿼리 결과를 보면 possible_keys 열은 서버가 idx_last_name 또는 idx_full_name 인덱스를 사용하도록 결정할 수 있음을 알려주고 키 열은 idx_full_name 인덱스가 선택되었음을 알려줍니다. 또한 유형 열은 범위 스캔이 활용될 것임을 알려줍니다. 즉, 데이터베이스 서버는 단일 행을 검색할 것으로 기대하지 않고 인덱스에서 값 범위를 찾습니다.

---
노트

방금 안내한 프로세스는 쿼리 튜닝의 예입니다. 튜닝에는 SQL 문을 살펴보고 해당 명령문을 실행하기 위해 서버에서 사용할 수 있는 리소스를 결정하는 작업이 포함됩니다. SQL 문을 수정하거나, 데이터베이스 리소스를 조정하거나, 명령문을 보다 효율적으로 실행하기 위해 둘 다 수행하도록 결정할 수 있습니다. 튜닝은 세부적인 주제이므로 서버의 튜닝 가이드를 읽거나 서버에 사용할 수 있는 다양한 접근 방식을 모두 볼 수 있는 좋은 튜닝 책을 선택하는 것이 좋습니다.

---

## 인덱스의 단점

색인이 그렇게 훌륭하다면 모든 것을 색인화하지 않는 이유는 무엇입니까? 더 많은 인덱스가 반드시 좋은 것은 아닌 이유를 이해하는 열쇠는 모든 인덱스가 테이블(특별한 유형의 테이블이지만 여전히 테이블)이라는 점을 명심하는 것입니다. 따라서 테이블에 행을 추가하거나 제거할 때마다 해당 테이블의 모든 인덱스를 수정해야 합니다. 행이 업데이트되면 영향을 받은 열의 인덱스도 수정해야 합니다. 따라서 인덱스가 많을수록 모든 스키마 개체를 최신 상태로 유지하기 위해 서버가 더 많은 작업을 수행해야 하므로 작업 속도가 느려지는 경향이 있습니다.

인덱스는 또한 디스크 공간과 어느 정도 관리자의 주의가 필요하므로 명확한 필요성이 발생할 때 인덱스를 추가하는 것이 가장 좋은 전략입니다. 월별 유지 관리 루틴과 같이 특별한 목적으로만 인덱스가 필요한 경우 인덱스를 추가하고 루틴을 실행한 다음 다시 필요할 때까지 인덱스를 삭제할 수 있습니다. 데이터 웨어하우스의 경우 사용자가 보고서 및 임시 쿼리를 실행하는 업무 시간 동안 인덱스가 중요하지만 데이터가 밤새 웨어하우스에 로드될 때 문제가 되는 데이터 웨어하우스의 경우 데이터가 로드되기 전에 인덱스를 삭제한 다음 창고가 영업을 시작하기 전에 다시 만드십시오.

- 일반적으로 인덱스가 너무 많지도 너무 적지도 않도록 노력해야 합니다. 인덱스가 몇 개인지 잘 모르겠다면 이 전략을 기본값으로 사용할 수 있습니다.

- 모든 기본 키 열이 인덱싱되었는지 확인하십시오(대부분의 서버는 기본 키 제약 조건을 생성할 때 자동으로 고유 인덱스를 생성합니다). 다중 열 기본 키의 경우 기본 키 열의 하위 집합 또는 모든 기본 키 열에서 기본 키 제약 조건 정의와 다른 순서로 추가 인덱스를 구축하는 것이 좋습니다.

- 외래 키 제약 조건에서 참조되는 모든 열에 인덱스를 작성합니다. 서버는 부모가 삭제될 때 자식 행이 없는지 확인하므로 열의 특정 값을 검색하기 위해 쿼리를 실행해야 합니다. 열에 인덱스가 없으면 전체 테이블을 스캔해야 합니다.

- 데이터 검색에 자주 사용되는 모든 열을 인덱싱하십시오. 대부분의 날짜 열은 짧은(2~50자) 문자열 열과 함께 좋은 후보입니다.

- 초기 인덱스 세트를 구축한 후 테이블에 대한 실제 쿼리를 캡처하고 서버의 실행 계획을 살펴보고 가장 일반적인 액세스 경로에 맞게 인덱싱 전략을 수정하십시오.

## 제약

제약 조건은 단순히 테이블의 하나 이상의 열에 적용되는 제한 사항입니다. 다음과 같은 여러 유형의 제약 조건이 있습니다.

- **기본 키 제약 조건**
테이블 내에서 고유성을 보장하는 열 식별

- **외래 키 제약 조건**
다른 테이블의 기본 키 열에 있는 값만 포함하도록 하나 이상의 열을 제한합니다(업데이트 캐스케이드 또는 캐스케이드 삭제 규칙이 설정된 경우 다른 테이블의 허용 가능한 값도 제한할 수 있음).

- **고유 제약 조건**
테이블 내에서 고유한 값을 포함하도록 하나 이상의 열을 제한합니다(기본 키 제약 조건은 고유 제약 조건의 특수한 유형임).

- **제약 조건 확인**
열에 허용되는 값 제한

제약 조건이 없으면 데이터베이스의 일관성이 의심됩니다. 예를 들어 서버에서 임대 테이블의 동일한 고객 ID를 변경하지 않고 고객 테이블의 고객 ID를 변경할 수 있도록 허용하는 경우 더 이상 유효한 고객 레코드(고아 행이라고 함)를 가리키지 않는 임대 데이터로 끝납니다. 그러나 기본 및 외래 키 제약 조건이 있는 경우 다른 테이블에서 참조하는 데이터를 수정 또는 삭제하거나 변경 사항을 다른 테이블로 전파하려고 하면 서버에서 오류가 발생합니다(자세한 내용은 곧 설명).
---
노트

MySQL 서버에서 외래 키 제약 조건을 사용하려면 테이블에 InnoDB 스토리지 엔진을 사용해야 합니다.

---

## Constraint Creation

Constraints are generally created at the same time as the associated table via the create table statement. To illustrate, here’s an example from the schema generation script for the Sakila sample database:

```sql
CREATE TABLE customer (
  customer_id SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
  store_id TINYINT UNSIGNED NOT NULL,
  first_name VARCHAR(45) NOT NULL,
  last_name VARCHAR(45) NOT NULL,
  email VARCHAR(50) DEFAULT NULL,
  address_id SMALLINT UNSIGNED NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  create_date DATETIME NOT NULL,
  last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP 
    ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (customer_id),
  KEY idx_fk_store_id (store_id),
  KEY idx_fk_address_id (address_id),
  KEY idx_last_name (last_name),
  CONSTRAINT fk_customer_address FOREIGN KEY (address_id) 
    REFERENCES address (address_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_customer_store FOREIGN KEY (store_id) 
    REFERENCES store (store_id) ON DELETE RESTRICT ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8;
```
고객 테이블에는 세 가지 제약 조건이 있습니다. 하나는 customer_id 열이 테이블의 기본 키로 사용하도록 지정하고 다른 두 개는 address_id 및 store_id 열이 주소 및 저장소 테이블에 대한 외래 키로 사용하도록 지정합니다. 또는 외래 키 제약 조건 없이 고객 테이블을 생성하고 테이블 변경 문을 통해 나중에 외래 키 제약 조건을 추가할 수 있습니다.

```sql
ALTER TABLE customer
ADD CONSTRAINT fk_customer_address FOREIGN KEY (address_id)
REFERENCES address (address_id) ON DELETE RESTRICT ON UPDATE CASCADE;
```
```sql
ALTER TABLE customer
ADD CONSTRAINT fk_customer_store FOREIGN KEY (store_id)
REFERENCES store (store_id) ON DELETE RESTRICT ON UPDATE CASCADE;
```

Both of these statements include several on clauses:

- on delete restrict, which will cause the server to raise an error if a row is deleted in the parent table (address or store) that is referenced in the child table (customer)
- on update cascade, which will cause the server to propagate a change to the primary key value of a parent table (address or store) to the child table (customer)

The on delete restrict clause protects against orphaned records when rows are deleted from the parent table. To illustrate, let’s pick a row in the address table and show the data from both the address and customer tables that share this value:

```sql
SELECT c.first_name, c.last_name, c.address_id, a.address
     FROM customer c
       INNER JOIN address a
       ON c.address_id = a.address_id
     WHERE a.address_id = 123;
```
```
+------------+-----------+------------+----------------------------------+
| first_name | last_name | address_id | address                          |
+------------+-----------+------------+----------------------------------+
| SHERRY     | MARSHALL  |        123 | 1987 Coacalco de Berriozbal Loop |
+------------+-----------+------------+----------------------------------+
1 row in set (0.00 sec)
```
The results show that there is a single customer row (for Sherry Marshall) whose address_id column contains the value 123.

Here’s what happens if you try to remove this row from the parent (address) table:

```sql
DELETE FROM address WHERE address_id = 123;
```
```
ERROR 1451 (23000): Cannot delete or update a parent row: 
  a foreign key constraint fails (`sakila`.`customer`, 
  CONSTRAINT `fk_customer_address` FOREIGN KEY (`address_id`) 
  REFERENCES `address` (`address_id`) 
  ON DELETE RESTRICT ON UPDATE CASCADE)
```
Because at least one row in the child table contains the value 123 in the address_id column, the on delete restrict clause of the foreign key constraint caused the statement to fail.

The on update cascade clause also protects against orphaned records when a primary key value is updated in the parent table using a different strategy. Here’s what happens if you modify a value in the address.address_id column:

```sql
UPDATE address
     SET address_id = 9999
     WHERE address_id = 123;
```
```
Query OK, 1 row affected (0.37 sec)
```
Rows matched: 1  Changed: 1  Warnings: 0
The statement executed without error, and one row was modified. But what happened to Sherry Marshall’s row in the customer table? Does it still point to address ID 123, which no longer exists? To find out, let’s run the last query again, but substitute the new value 9999 for the previous value of 123:

```sql
SELECT c.first_name, c.last_name, c.address_id, a.address
     FROM customer c
       INNER JOIN address a
       ON c.address_id = a.address_id
     WHERE a.address_id = 9999;
```
```
+------------+-----------+------------+----------------------------------+
| first_name | last_name | address_id | address                          |
+------------+-----------+------------+----------------------------------+
| SHERRY     | MARSHALL  |       9999 | 1987 Coacalco de Berriozbal Loop |
+------------+-----------+------------+----------------------------------+
1 row in set (0.00 sec)
```
보시다시피 이전과 동일한 결과가 반환됩니다(새 주소 ID 값 제외). 이는 값 9999가 고객 테이블에서 자동으로 업데이트되었음을 의미합니다. 이것을 캐스케이드라고 하며 고아 행을 보호하는 데 사용되는 두 번째 메커니즘입니다.

제한 및 계단식 배열과 함께 null 설정을 선택할 수도 있습니다. 그러면 부모 테이블에서 행이 삭제되거나 업데이트될 때 자식 테이블에서 외래 키 값이 null로 설정됩니다. 외래 키 제약 조건을 정의할 때 선택할 수 있는 옵션은 모두 6가지입니다.
```
on delete restrict
on delete cascade
on delete set null
on update restrict
on update cascade
on update set null
```
이는 선택 사항이므로 외래 키 제약 조건을 정의할 때 0개, 1개 또는 2개(삭제 시 하나, 업데이트 시 하나)를 선택할 수 있습니다.

마지막으로 기본 또는 외래 키 제약 조건을 제거하려면 add 대신 drop을 지정하는 것을 제외하고 alter table 문을 다시 사용할 수 있습니다. 기본 키 제약 조건을 삭제하는 것은 드문 일이지만 외래 키 제약 조건은 특정 유지 관리 작업 중에 삭제된 다음 다시 설정되는 경우가 있습니다.

## Test Your Knowledge

Work through the following exercises to test your knowledge of indexes and constraints. When you’re done, compare your solutions with those in Appendix B.

Exercise 13-1
Generate an alter table statement for the rental table so that an error will be raised if a row having a value found in the rental.customer_id column is deleted from the customer table.

Exercise 13-2
Generate a multicolumn index on the payment table that could be used by both of the following queries:

SELECT customer_id, payment_date, amount
FROM payment
WHERE payment_date > cast('2019-12-31 23:59:59' as datetime);
```
```

SELECT customer_id, payment_date, amount
FROM payment
​WHERE payment_date > cast('2019-12-31 23:59:59' as datetime)
  AND amount < 5;
```
```
