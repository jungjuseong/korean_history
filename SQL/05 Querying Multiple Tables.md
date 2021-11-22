
# Chapter 5. Querying Multiple Tables

Back in Chapter 2, I demonstrated how related concepts are broken into separate pieces through a process known as normalization. The end result of this exercise was two tables: person and favorite_food. If, however, you want to generate a single report showing a person’s name, address, and favorite foods, you will need a mechanism to bring the data from these two tables back together again; this mechanism is known as a join, and this chapter concentrates on the simplest and most common join, the inner join. Chapter 10 demonstrates all of the different join types.

## What Is a Join?
Queries against a single table are certainly not rare, but you will find that most of your queries will require two, three, or even more tables. To illustrate, let’s look at the definitions for the customer and address tables and then define a query that retrieves data from both tables:

```
mysql> desc customer;
+-------------+----------------------+------+-----+-------------------+
| Field       | Type                 | Null | Key | Default           |
+-------------+----------------------+------+-----+-------------------+
| customer_id | smallint(5) unsigned | NO   | PRI | NULL              |
| store_id    | tinyint(3) unsigned  | NO   | MUL | NULL              |
| first_name  | varchar(45)          | NO   |     | NULL              |
| last_name   | varchar(45)          | NO   | MUL | NULL              |
| email       | varchar(50)          | YES  |     | NULL              |
| address_id  | smallint(5) unsigned | NO   | MUL | NULL              |
| active      | tinyint(1)           | NO   |     | 1                 |
| create_date | datetime             | NO   |     | NULL              |
| last_update | timestamp            | YES  |     | CURRENT_TIMESTAMP |
+-------------+----------------------+------+-----+-------------------+

mysql> desc address;
+-------------+----------------------+------+-----+-------------------+
| Field       | Type                 | Null | Key | Default           |
+-------------+----------------------+------+-----+-------------------+
| address_id  | smallint(5) unsigned | NO   | PRI | NULL              |
| address     | varchar(50)          | NO   |     | NULL              |
| address2    | varchar(50)          | YES  |     | NULL              |
| district    | varchar(20)          | NO   |     | NULL              |
| city_id     | smallint(5) unsigned | NO   | MUL | NULL              |
| postal_code | varchar(10)          | YES  |     | NULL              |
| phone       | varchar(20)          | NO   |     | NULL              |
| location    | geometry             | NO   | MUL | NULL              |
| last_update | timestamp            | NO   |     | CURRENT_TIMESTAMP |
+-------------+----------------------+------+-----+-------------------+
```

각 고객의 이름과 성을 주소와 함께 검색한다고 가정해 보겠습니다. 따라서 쿼리는 customer.first_name, customer.last_name 및 address.address 열을 검색해야 합니다. 그러나 동일한 쿼리의 두 테이블에서 데이터를 검색하는 방법은 무엇입니까? 답은 주소 테이블에 있는 고객 레코드의 ID를 보유하는 customer.address_id 열에 있습니다(좀 더 공식적인 용어로 customer.address_id 열은 주소 테이블에 대한 외래 키입니다). 곧 보게 될 쿼리는 고객과 주소 테이블 간의 이동 수단으로 customer.address_id 열을 사용하도록 서버에 지시하므로 두 테이블의 열이 쿼리의 결과 집합에 포함될 수 있습니다. 이러한 유형의 작업을 조인이라고 합니다.

---
NOTE

한 테이블의 값이 다른 테이블에 존재하는지 확인하기 위해 외래 키 제약 조건을 선택적으로 생성할 수 있습니다. 이전 예의 경우 customer.address_id 열에 삽입된 모든 값을 address.address_id 열에서 찾을 수 있도록 외래 키 제약 조건을 customer 테이블에 생성할 수 있습니다. 두 테이블을 조인하기 위해 외래 키 제약 조건이 있을 필요는 없습니다.

---

### Cartesian Product

시작하는 가장 쉬운 방법은 고객 및 주소 테이블을 쿼리의 from 절에 넣고 어떤 일이 발생하는지 확인하는 것입니다. 다음은 join 키워드로 구분된 두 테이블의 이름을 지정하는 from 절을 사용하여 고객의 이름과 성을 거리 주소와 함께 검색하는 쿼리입니다.

```sql
mysql> SELECT c.first_name, c.last_name, a.address
    -> FROM customer c JOIN address a;
```

```
+------------+-----------+----------------------+
| first_name | last_name | address              |
+------------+-----------+----------------------+
| MARY       | SMITH     | 47 MySakila Drive    |
| PATRICIA   | JOHNSON   | 47 MySakila Drive    |
| LINDA      | WILLIAMS  | 47 MySakila Drive    |
| BARBARA    | JONES     | 47 MySakila Drive    |
| ELIZABETH  | BROWN     | 47 MySakila Drive    |
| JENNIFER   | DAVIS     | 47 MySakila Drive    |
| MARIA      | MILLER    | 47 MySakila Drive    |
| SUSAN      | WILSON    | 47 MySakila Drive    |
...
| SETH       | HANNON    | 1325 Fukuyama Street |
| KENT       | ARSENAULT | 1325 Fukuyama Street |
| TERRANCE   | ROUSH     | 1325 Fukuyama Street |
| RENE       | MCALISTER | 1325 Fukuyama Street |
| EDUARDO    | HIATT     | 1325 Fukuyama Street |
| TERRENCE   | GUNDERSON | 1325 Fukuyama Street |
| ENRIQUE    | FORSYTHE  | 1325 Fukuyama Street |
| FREDDIE    | DUGGAN    | 1325 Fukuyama Street |
| WADE       | DELVALLE  | 1325 Fukuyama Street |
| AUSTIN     | CINTRON   | 1325 Fukuyama Street |
+------------+-----------+----------------------+
361197 rows in set (0.03 sec)
```

흠...주소 테이블에는 599명의 고객과 603개의 행만 있는데 결과 집합은 어떻게 361,197행으로 끝났습니까? 더 자세히 살펴보면 많은 고객이 동일한 주소를 사용하는 것처럼 보이는 것을 알 수 있습니다. 쿼리가 두 테이블을 조인하는 방법을 지정하지 않았기 때문에 데이터베이스 서버는 두 테이블의 모든 순열인 카테지안 곱을 생성했습니다(599명의 고객 x 603개의 주소 = 361,197개의 순열). 이러한 유형의 조인을 크로스 조인이라고 하며 거의 사용되지 않습니다(적어도 의도적으로). 크로스 조인은 10장에서 공부하는 조인 유형 중 하나입니다.

### 내부 조인
각 고객에 대해 단일 행만 반환되도록 이전 쿼리를 수정하려면 두 테이블이 어떻게 관련되어 있는지 설명해야 합니다. 앞서 나는 customer.address_id 컬럼이 두 테이블 사이의 링크 역할을 한다는 것을 보여주었기 때문에 이 정보는 from 절의 on 하위 절에 추가되어야 합니다.

```sql
mysql> SELECT c.first_name, c.last_name, a.address
    -> FROM customer c JOIN address a
    ->   ON c.address_id = a.address_id;
```
```
+-------------+--------------+----------------------------------------+
| first_name  | last_name    | address                                |
+-------------+--------------+----------------------------------------+
| MARY        | SMITH        | 1913 Hanoi Way                         |
| PATRICIA    | JOHNSON      | 1121 Loja Avenue                       |
| LINDA       | WILLIAMS     | 692 Joliet Street                      |
| BARBARA     | JONES        | 1566 Inegl Manor                       |
| ELIZABETH   | BROWN        | 53 Idfu Parkway                        |
| JENNIFER    | DAVIS        | 1795 Santiago de Compostela Way        |
| MARIA       | MILLER       | 900 Santiago de Compostela Parkway     |
| SUSAN       | WILSON       | 478 Joliet Way                         |
| MARGARET    | MOORE        | 613 Korolev Drive                      |
...
| TERRANCE    | ROUSH        | 42 Fontana Avenue                      |
| RENE        | MCALISTER    | 1895 Zhezqazghan Drive                 |
| EDUARDO     | HIATT        | 1837 Kaduna Parkway                    |
| TERRENCE    | GUNDERSON    | 844 Bucuresti Place                    |
| ENRIQUE     | FORSYTHE     | 1101 Bucuresti Boulevard               |
| FREDDIE     | DUGGAN       | 1103 Quilmes Boulevard                 |
| WADE        | DELVALLE     | 1331 Usak Boulevard                    |
| AUSTIN      | CINTRON      | 1325 Fukuyama Street                   |
+-------------+--------------+----------------------------------------+
599 rows in set (0.00 sec)
```

361,197개 행 대신에 이제 on 하위 절이 추가되어 예상되는 599개 행이 있습니다. 이 절은 address_id 열을 사용하여 한 테이블에서 다른 테이블로 이동하여 고객 및 주소 테이블을 조인하도록 서버에 지시합니다. 예를 들어, 고객 테이블의 Mary Smith 행은 address_id 열의 값 5를 포함합니다(예제에는 표시되지 않음). 서버는 이 값을 사용하여 address_id 열의 값이 5인 주소 테이블의 행을 찾은 다음 해당 행의 주소 열에서 '1913 Hanoi Way' 값을 검색합니다.

한 테이블의 address_id 열에 대한 값이 존재하지만 다른 테이블에는 없는 경우 해당 값을 포함하는 행에 대한 조인이 실패하고 해당 행이 결과 집합에서 제외됩니다. 이러한 유형의 조인을 내부 조인이라고 하며 가장 일반적으로 사용되는 조인 유형입니다. 명확히 하자면, 고객 테이블의 행에 address_id 열의 값이 999이고 address_id 열의 값이 999인 행이 주소 테이블에 없는 경우 해당 고객 행은 결과 집합에 포함되지 않습니다. 일치 여부에 관계없이 한 테이블 또는 다른 테이블의 모든 행을 포함하려면 외부 조인을 지정해야 하지만 이에 대해서는 10장에서 살펴봅니다.

이전 예에서는 from 절에서 사용할 조인 유형을 지정하지 않았습니다. 그러나 내부 조인을 사용하여 두 테이블을 조인하려면 from 절에 이를 명시적으로 지정해야 합니다. 다음은 조인 유형이 추가된 동일한 예입니다(inner 키워드 참고).

```sql
SELECT c.first_name, c.last_name, a.address
FROM customer c INNER JOIN address a
  ON c.address_id = a.address_id;
```

조인 유형을 지정하지 않으면 서버는 기본적으로 내부 조인을 수행합니다. 그러나 이 책의 뒷부분에서 볼 수 있듯이 조인에는 여러 유형이 있으므로 특히 자신을 사용/유지할 수 있는 다른 사람들의 이익을 위해 필요한 정확한 조인 유형을 지정하는 습관을 가져야 합니다.

두 테이블을 조인하는 데 사용된 열의 이름이 동일한 경우(이전 쿼리에서 참) 다음과 같이 on 하위 절 대신 using 하위 절을 사용할 수 있습니다.

```sql
SELECT c.first_name, c.last_name, a.address
FROM customer c INNER JOIN address a USING (address_id);
```

Since using is a shorthand notation that you can use in only a specific situation, I prefer always to use the `on` subclause to avoid confusion.

### The ANSI Join Syntax

이 책 전체에서 테이블을 조인하기 위해 사용된 표기법은 ANSI SQL 표준의 SQL92 버전에서 도입되었습니다. 모든 주요 데이터베이스(Oracle Database, Microsoft SQL Server, MySQL, IBM DB2 Universal Database 및 Sybase Adaptive Server)는 SQL92 조인 구문을 채택했습니다. 이러한 서버의 대부분은 SQL92 사양의 릴리스 이전부터 사용되었기 때문에 모두 이전 조인 구문도 포함합니다. 예를 들어, 이러한 모든 서버는 이전 쿼리의 다음 변형을 이해합니

```sql
mysql> SELECT c.first_name, c.last_name, a.address
    -> FROM customer c, address a
    -> WHERE c.address_id = a.address_id;
```
```
+------------+------------+------------------------------------+
| first_name | last_name  | address                            |
+------------+------------+------------------------------------+
| MARY       | SMITH      | 1913 Hanoi Way                     |
| PATRICIA   | JOHNSON    | 1121 Loja Avenue                   |
| LINDA      | WILLIAMS   | 692 Joliet Street                  |
| BARBARA    | JONES      | 1566 Inegl Manor                   |
| ELIZABETH  | BROWN      | 53 Idfu Parkway                    |
| JENNIFER   | DAVIS      | 1795 Santiago de Compostela Way    |
| MARIA      | MILLER     | 900 Santiago de Compostela Parkway |
| SUSAN      | WILSON     | 478 Joliet Way                     |
| MARGARET   | MOORE      | 613 Korolev Drive                  |
...
| TERRANCE   | ROUSH      | 42 Fontana Avenue                  |
| RENE       | MCALISTER  | 1895 Zhezqazghan Drive             |
| EDUARDO    | HIATT      | 1837 Kaduna Parkway                |
| TERRENCE   | GUNDERSON  | 844 Bucuresti Place                |
| ENRIQUE    | FORSYTHE   | 1101 Bucuresti Boulevard           |
| FREDDIE    | DUGGAN     | 1103 Quilmes Boulevard             |
| WADE       | DELVALLE   | 1331 Usak Boulevard                |
| AUSTIN     | CINTRON    | 1325 Fukuyama Street               |
+------------+------------+------------------------------------+
599 rows in set (0.00 sec)
```
조인을 지정하는 이 이전 방법에는 on 하위 절이 포함되지 않습니다. 대신, 테이블은 쉼표로 구분된 from 절에 이름이 지정되고 조인 조건은 where 절에 포함됩니다. 이전 조인 구문을 위해 SQL92 구문을 무시하기로 결정할 수 있지만 ANSI 조인 구문에는 다음과 같은 이점이 있습니다.

조인 조건과 필터 조건은 두 개의 다른 절(각각 on 하위 절과 where 절)로 분리되어 쿼리를 더 쉽게 이해할 수 있습니다.

각 테이블 쌍에 대한 조인 조건은 자체 on 절에 포함되어 있어 조인의 일부가 실수로 생략될 가능성이 줄어듭니다.

SQL92 조인 구문을 사용하는 쿼리는 데이터베이스 서버 간에 이식 가능하지만 이전 구문은 서버마다 약간 다릅니다.

SQL92 조인 구문의 이점은 조인 및 필터 조건을 모두 포함하는 복잡한 쿼리에 대해 더 쉽게 식별할 수 있습니다. 우편 번호가 52137인 고객만 반환하는 다음 쿼리를 고려하십시오.

```sql
mysql> SELECT c.first_name, c.last_name, a.address
    -> FROM customer c, address a
    -> WHERE c.address_id = a.address_id
    ->   AND a.postal_code = 52137;
```
```
+------------+-----------+------------------------+
| first_name | last_name | address                |
+------------+-----------+------------------------+
| JAMES      | GANNON    | 1635 Kuwana Boulevard  |
| FREDDIE    | DUGGAN    | 1103 Quilmes Boulevard |
+------------+-----------+------------------------+
2 rows in set (0.01 sec)
```
얼핏 보면 where 절의 어떤 조건이 조인 조건이고 어떤 조건이 필터 조건인지 판단하기가 쉽지 않습니다. 또한 어떤 유형의 조인이 사용되고 있는지 쉽게 알 수 없으며(조인 유형을 식별하려면 특수 문자가 사용되는지 여부를 확인하기 위해 where 절의 조인 조건을 자세히 살펴보아야 합니다) 조인 조건이 실수로 누락되었는지 여부를 확인합니다. 다음은 SQL92 조인 구문을 사용하는 동일한 쿼리입니다.

```sql
mysql> SELECT c.first_name, c.last_name, a.address
    -> FROM customer c INNER JOIN address a
    ->   ON c.address_id = a.address_id
    -> WHERE a.postal_code = 52137;
```
```
+------------+-----------+------------------------+
| first_name | last_name | address                |
+------------+-----------+------------------------+
| JAMES      | GANNON    | 1635 Kuwana Boulevard  |
| FREDDIE    | DUGGAN    | 1103 Quilmes Boulevard |
+------------+-----------+------------------------+
2 rows in set (0.00 sec)
```

With this version, it is clear which condition is used for the join and which condition is used for filtering. Hopefully, you will agree that the version using SQL92 join syntax is easier to understand.

### 3 이상의 테이블 조인

세 개의 테이블을 결합하는 것은 두 개의 테이블을 결합하는 것과 유사하지만 하나의 약간의 주름이 있습니다. 두 개의 테이블 조인을 사용하면 from 절에 두 개의 테이블과 하나의 조인 유형이 있으며 테이블이 조인되는 방법을 정의하는 하위 절에 단일 on이 있습니다. 3개 테이블 조인을 사용하면 from 절에 3개의 테이블과 2개의 조인 유형이 있고 하위 절에 2개가 있습니다.

설명을 위해 이전 쿼리를 변경하여 고객의 주소가 아닌 도시를 반환해 보겠습니다. 그러나 도시 이름은 주소 테이블에 저장되지 않고 도시 테이블에 대한 외래 키를 통해 액세스됩니다. 다음은 테이블 정의입니다.

```sql
mysql> desc address;
```

```
+-------------+----------------------+------+-----+-------------------+
| Field       | Type                 | Null | Key | Default           |
+-------------+----------------------+------+-----+-------------------+
| address_id  | smallint(5) unsigned | NO   | PRI | NULL              |
| address     | varchar(50)          | NO   |     | NULL              |
| address2    | varchar(50)          | YES  |     | NULL              |
| district    | varchar(20)          | NO   |     | NULL              |
| city_id     | smallint(5) unsigned | NO   | MUL | NULL              |
| postal_code | varchar(10)          | YES  |     | NULL              |
| phone       | varchar(20)          | NO   |     | NULL              |
| location    | geometry             | NO   | MUL | NULL              |
| last_update | timestamp            | NO   |     | CURRENT_TIMESTAMP |
+-------------+----------------------+------+-----+-------------------+
```
```sql
mysql> desc city;
```
```
+-------------+----------------------+------+-----+-------------------+
| Field       | Type                 | Null | Key | Default           |
+-------------+----------------------+------+-----+-------------------+
| city_id     | smallint(5) unsigned | NO   | PRI | NULL              |
| city        | varchar(50)          | NO   |     | NULL              |
| country_id  | smallint(5) unsigned | NO   | MUL | NULL              |
| last_update | timestamp            | NO   |     | CURRENT_TIMESTAMP |
+-------------+----------------------+------+-----+-------------------+
```
각 고객의 도시를 표시하려면 address_id 열을 사용하여 고객 테이블에서 주소 테이블로 이동한 다음 city_id 열을 사용하여 주소 테이블에서 도시 테이블로 이동해야 합니다. 

쿼리는 다음과 같습니다.
```
mysql> SELECT c.first_name, c.last_name, ct.city
    -> FROM customer c
    ->   INNER JOIN address a
    ->   ON c.address_id = a.address_id
    ->   INNER JOIN city ct
    ->   ON a.city_id = ct.city_id;
```

```
+-------------+--------------+----------------------------+
| first_name  | last_name    | city                       |
+-------------+--------------+----------------------------+
| JULIE       | SANCHEZ      | A Corua (La Corua)         |
| PEGGY       | MYERS        | Abha                       |
| TOM         | MILNER       | Abu Dhabi                  |
| GLEN        | TALBERT      | Acua                       |
| LARRY       | THRASHER     | Adana                      |
| SEAN        | DOUGLASS     | Addis Abeba                |
...
| MICHELE     | GRANT        | Yuncheng                   |
| GARY        | COY          | Yuzhou                     |
| PHYLLIS     | FOSTER       | Zalantun                   |
| CHARLENE    | ALVAREZ      | Zanzibar                   |
| FRANKLIN    | TROUTMAN     | Zaoyang                    |
| FLOYD       | GANDY        | Zapopan                    |
| CONSTANCE   | REID         | Zaria                      |
| JACK        | FOUST        | Zeleznogorsk               |
| BYRON       | BOX          | Zhezqazghan                |
| GUY         | BROWNLEE     | Zhoushan                   |
| RONNIE      | RICKETTS     | Ziguinchor                 |
+-------------+--------------+----------------------------+
599 rows in set (0.03 sec)
```
이 쿼리의 경우 from 절에 3개의 테이블, 2개의 조인 유형 및 2개의 on 하위 절이 있으므로 상황이 상당히 바빠졌습니다. 언뜻 보면 from 절에서 테이블이 나타나는 순서가 중요한 것처럼 보일 수 있지만 테이블 순서를 전환하면 똑같은 결과를 얻을 수 있습니다. 이 세 가지 변형은 모두 동일한 결과를 반환합니다.

```sql
SELECT c.first_name, c.last_name, ct.city
FROM customer c
  INNER JOIN address a
  ON c.address_id = a.address_id
  INNER JOIN city ct
  ON a.city_id = ct.city_id;

SELECT c.first_name, c.last_name, ct.city
FROM city ct
  INNER JOIN address a
  ON a.city_id = ct.city_id
  INNER JOIN customer c
  ON c.address_id = a.address_id;

SELECT c.first_name, c.last_name, ct.city
FROM address a
  INNER JOIN city ct
  ON a.city_id = ct.city_id
  INNER JOIN customer c
  ON c.address_id = a.address_id;

```
결과를 정렬하는 방법을 지정하는 order by 절이 없기 때문에 볼 수 있는 유일한 차이점은 행이 반환되는 순서입니다.

DOES JOIN ORDER MATTER?

고객/주소/도시 쿼리의 세 가지 버전이 모두 동일한 결과를 생성하는 이유에 대해 혼란스럽다면 SQL이 비절차적 언어라는 점을 명심하십시오. 그러나 쿼리를 가장 잘 실행하는 방법을 결정하는 것은 데이터베이스 서버에 달려 있습니다. 데이터베이스 개체에서 수집한 통계를 사용하여 서버는 세 개의 테이블 중 하나를 시작점으로 선택한 다음(선택한 테이블을 이후 구동 테이블이라고 함) 나머지 테이블을 조인할 순서를 결정해야 합니다. 따라서 from 절에 테이블이 나타나는 순서는 중요하지 않습니다.

그러나 쿼리의 테이블이 항상 특정 순서로 조인되어야 한다고 생각되면 테이블을 원하는 순서로 배치한 다음 MySQL에서 키워드 straight_join을 지정하거나 SQL Server에서 강제 순서 옵션을 요청하거나 다음을 사용할 수 있습니다. Oracle Database의 순서 또는 선행 옵티마이저 힌트입니다. 예를 들어 MySQL 서버에 도시 테이블을 구동 테이블로 사용하고 주소 및 고객 테이블을 조인하도록 지시하려면 다음을 수행할 수 있습니다.

```sql
SELECT STRAIGHT_JOIN c.first_name, c.last_name, ct.city
FROM city ct
  INNER JOIN address a
  ON a.city_id = ct.city_id
  INNER JOIN customer c
  ON c.address_id = a.address_id
```

### 서브쿼리를 테이블처럼 사용

여러 테이블을 포함하는 쿼리의 몇 가지 예를 이미 보았지만 언급할 가치가 있는 한 가지 변형이 있습니다. 일부 데이터 세트가 하위 쿼리에 의해 생성된 경우 수행할 작업입니다. 서브쿼리는 9장에서 중점적으로 다루지만 이미 앞 장에서 from 절에서 서브쿼리의 개념을 소개했습니다. 

다음 쿼리는 고객 테이블을 주소 및 도시 테이블에 대한 하위 쿼리에 조인합니다.

```sql
mysql> SELECT c.first_name, c.last_name, addr.address, addr.city
    -> FROM customer c
    ->   INNER JOIN
    ->    (SELECT a.address_id, a.address, ct.city
    ->     FROM address a
    ->       INNER JOIN city ct
    ->       ON a.city_id = ct.city_id
    ->     WHERE a.district = 'California'
    ->    ) addr
    ->   ON c.address_id = addr.address_id;
```

```
+------------+-----------+------------------------+----------------+
| first_name | last_name | address                | city           |
+------------+-----------+------------------------+----------------+
| PATRICIA   | JOHNSON   | 1121 Loja Avenue       | San Bernardino |
| BETTY      | WHITE     | 770 Bydgoszcz Avenue   | Citrus Heights |
| ALICE      | STEWART   | 1135 Izumisano Parkway | Fontana        |
| ROSA       | REYNOLDS  | 793 Cam Ranh Avenue    | Lancaster      |
| RENEE      | LANE      | 533 al-Ayn Boulevard   | Compton        |
| KRISTIN    | JOHNSTON  | 226 Brest Manor        | Sunnyvale      |
| CASSANDRA  | WALTERS   | 920 Kumbakonam Loop    | Salinas        |
| JACOB      | LANCE     | 1866 al-Qatif Avenue   | El Monte       |
| RENE       | MCALISTER | 1895 Zhezqazghan Drive | Garden Grove   |
+------------+-----------+------------------------+----------------+
9 rows in set (0.00 sec)
```
4행에서 시작하고 별칭으로 addr이 지정된 서브 쿼리는 캘리포니아에 있는 모든 주소를 찾습니다. 외부 쿼리는 서브 쿼리 결과를 고객 테이블에 조인하여 캘리포니아에 거주하는 모든 고객의 이름, 성, 주소 및 도시를 반환합니다. 이 쿼리는 단순히 세 개의 테이블을 조인하여 서브 쿼리를 사용하지 않고 작성할 수 있지만, 때때로 하나 이상의 서브 쿼리를 사용하는 것이 성능 및/또는 가독성 측면에서 유리할 수 있습니다.

진행 중인 일을 시각화하는 한 가지 방법은 하위 쿼리를 자체적으로 실행하고 결과를 보는 것입니다.

다음은 이전 예제의 하위 쿼리 결과입니다.

```sql
mysql> SELECT a.address_id, a.address, ct.city
    -> FROM address a
    ->   INNER JOIN city ct
    ->   ON a.city_id = ct.city_id
    -> WHERE a.district = 'California';
```
```
+------------+------------------------+----------------+
| address_id | address                | city           |
+------------+------------------------+----------------+
|          6 | 1121 Loja Avenue       | San Bernardino |
|         18 | 770 Bydgoszcz Avenue   | Citrus Heights |
|         55 | 1135 Izumisano Parkway | Fontana        |
|        116 | 793 Cam Ranh Avenue    | Lancaster      |
|        186 | 533 al-Ayn Boulevard   | Compton        |
|        218 | 226 Brest Manor        | Sunnyvale      |
|        274 | 920 Kumbakonam Loop    | Salinas        |
|        425 | 1866 al-Qatif Avenue   | El Monte       |
|        599 | 1895 Zhezqazghan Drive | Garden Grove   |
+------------+------------------------+----------------+
9 rows in set (0.00 sec)
```
This result set consists of all nine California addresses. When joined to the customer table via the address_id column, your result set will contain information about the customers assigned to these addresses.

### 같은 테이블을 두번 사용

여러 테이블을 조인하는 경우 동일한 테이블을 두 번 이상 조인해야 할 수 있습니다. 예를 들어 샘플 데이터베이스에서 배우는 film_actor 테이블을 통해 출연한 영화와 관련이 있습니다. 두 명의 특정 배우가 등장하는 모든 영화를 찾으려면 다음과 같은 쿼리를 작성할 수 있습니다.

```sql
mysql> SELECT f.title
    -> FROM film f
    ->   INNER JOIN film_actor fa
    ->   ON f.film_id = fa.film_id
    ->   INNER JOIN actor a
    ->   ON fa.actor_id = a.actor_id
    -> WHERE ((a.first_name = 'CATE' AND a.last_name = 'MCQUEEN')
    ->     OR (a.first_name = 'CUBA' AND a.last_name = 'BIRCH'));
```
```
+----------------------+
| title                |
+----------------------+
| ATLANTIS CAUSE       |
| BLOOD ARGONAUTS      |
| COMMANDMENTS EXPRESS |
| DYNAMITE TARZAN      |
| EDGE KISSING         |
...
| TOWERS HURRICANE     |
| TROJAN TOMORROW      |
| VIRGIN DAISY         |
| VOLCANO TEXAS        |
| WATERSHIP FRONTIER   |
+----------------------+
54 rows in set (0.00 sec)
```

이 쿼리는 Cate McQueen 또는 Cuba Birch가 등장한 모든 영화를 반환합니다. 그러나 두 배우가 모두 출연한 영화만 검색한다고 가정해 보겠습니다. 이를 수행하려면 film_actor 테이블에 두 개의 행이 있는 film 테이블의 모든 행을 찾아야 합니다. 그 중 하나는 Cate McQueen과 연결되고 다른 하나는 Cuba Birch와 연결됩니다. 따라서 서버가 다양한 절에서 어떤 테이블을 참조하는지 알 수 있도록 각각 다른 별칭을 가진 film_actor 및 Actor 테이블을 두 번 포함해야 합니다.
```sql
mysql> SELECT f.title
    ->  FROM film f
    ->    INNER JOIN film_actor fa1
    ->    ON f.film_id = fa1.film_id
    ->    INNER JOIN actor a1
    ->    ON fa1.actor_id = a1.actor_id
    ->    INNER JOIN film_actor fa2
    ->    ON f.film_id = fa2.film_id
    ->    INNER JOIN actor a2
    ->    ON fa2.actor_id = a2.actor_id
    -> WHERE (a1.first_name = 'CATE' AND a1.last_name = 'MCQUEEN')
    ->   AND (a2.first_name = 'CUBA' AND a2.last_name = 'BIRCH');
```
```
+------------------+
| title            |
+------------------+
| BLOOD ARGONAUTS  |
| TOWERS HURRICANE |
+------------------+
2 rows in set (0.00 sec)
```

그 사이 두 배우는 52편의 다른 영화에 출연했지만 두 배우가 모두 출연한 작품은 단 2편에 불과하다. 이것은 동일한 테이블이 여러 번 사용되기 때문에 테이블 별칭을 사용해야 하는 쿼리의 한 예입니다.


## 자체 조인

동일한 쿼리에 동일한 테이블을 두 번 이상 포함할 수 있을 뿐만 아니라 실제로 테이블 자체를 조인할 수 있습니다. 이것은 처음에는 이상한 일처럼 보일 수 있지만 그렇게 하는 데에는 타당한 이유가 있습니다. 일부 테이블에는 자체 참조 외래 키가 포함되어 있습니다. 즉, 동일한 테이블 내의 기본 키를 가리키는 열이 포함됩니다. 샘플 데이터베이스에는 이러한 관계가 포함되어 있지 않지만 영화 테이블에 영화의 상위 항목을 가리키는 prequel_film_id 열이 포함되어 있다고 가정해 보겠습니다(예: 영화 Fiddler Lost II는 이 열을 사용하여 상위 영화 Fiddler Lost를 가리킵니다). . 이 추가 열을 추가하면 테이블이 다음과 같이 표시됩니다.

```sql
mysql> desc film;
```
```
+----------------------+-----------------------+------+-----+-------------------+
| Field                | Type                  | Null | Key | Default           |
+----------------------+-----------------------+------+-----+-------------------+
| film_id              | smallint(5) unsigned  | NO   | PRI | NULL              |
| title                | varchar(255)          | NO   | MUL | NULL              |
| description          | text                  | YES  |     | NULL              |
| release_year         | year(4)               | YES  |     | NULL              |
| language_id          | tinyint(3) unsigned   | NO   | MUL | NULL              |
| original_language_id | tinyint(3) unsigned   | YES  | MUL | NULL              |
| rental_duration      | tinyint(3) unsigned   | NO   |     | 3                 |
| rental_rate          | decimal(4,2)          | NO   |     | 4.99              |
| length               | smallint(5) unsigned  | YES  |     | NULL              |
| replacement_cost     | decimal(5,2)          | NO   |     | 19.99             |
| rating               | enum('G','PG','PG-13',
                           'R','NC-17')        | YES  |     | G                 |
| special_features     | set('Trailers',...,
                           'Behind the Scenes')| YES  |     | NULL              |
| last_update          | timestamp             | NO   |     | CURRENT_
                                                                TIMESTAMP       |
| prequel_film_id      | smallint(5) unsigned  | YES  | MUL | NULL              |
+----------------------+-----------------------+------+-----+-------------------+
```

Using a self-join, you can write a query that lists every film that has a prequel, along with the prequel’s title:

```sql
mysql> SELECT f.title, f_prnt.title prequel
    -> FROM film f
    ->   INNER JOIN film f_prnt
    ->   ON f_prnt.film_id = f.prequel_film_id
    -> WHERE f.prequel_film_id IS NOT NULL;
```
```
+-----------------+--------------+
| title           | prequel      |
+-----------------+--------------+
| FIDDLER LOST II | FIDDLER LOST |
+-----------------+--------------+
1 row in set (0.00 sec)
```

이 쿼리는 prequel_film_id 외래 키를 사용하여 영화 테이블을 자체에 조인하고 테이블 별칭 f 및 f_prnt를 할당하여 어떤 테이블이 어떤 용도로 사용되는지 명확히 합니다.

## 지식 테스트

다음 연습은 내부 조인에 대한 이해도를 테스트하기 위해 고안되었습니다. 이러한 연습에 대한 솔루션은 부록 B를 참조하십시오.

**Exercise 5-1** 
Fill in the blanks (denoted by <#>) for the following query to obtain the results that follow:

```sql
mysql> SELECT c.first_name, c.last_name, a.address, ct.city
    -> FROM customer c
    ->   INNER JOIN address <1>
    ->   ON c.address_id = a.address_id
    ->   INNER JOIN city ct
    ->   ON a.city_id = <2>
    -> WHERE a.district = 'California';
```
```
+------------+-----------+------------------------+----------------+
| first_name | last_name | address                | city           |
+------------+-----------+------------------------+----------------+
| PATRICIA   | JOHNSON   | 1121 Loja Avenue       | San Bernardino |
| BETTY      | WHITE     | 770 Bydgoszcz Avenue   | Citrus Heights |
| ALICE      | STEWART   | 1135 Izumisano Parkway | Fontana        |
| ROSA       | REYNOLDS  | 793 Cam Ranh Avenue    | Lancaster      |
| RENEE      | LANE      | 533 al-Ayn Boulevard   | Compton        |
| KRISTIN    | JOHNSTON  | 226 Brest Manor        | Sunnyvale      |
| CASSANDRA  | WALTERS   | 920 Kumbakonam Loop    | Salinas        |
| JACOB      | LANCE     | 1866 al-Qatif Avenue   | El Monte       |
| RENE       | MCALISTER | 1895 Zhezqazghan Drive | Garden Grove   |
+------------+-----------+------------------------+----------------+
9 rows in set (0.00 sec)
```

**Exercise 5-2**
Write a query that returns the title of every film in which an actor with the first name JOHN appeared.

**Exercise 5-3**
Construct a query that returns all addresses that are in the same city. You will need to join the address table to itself, and each row should include two different addresses.