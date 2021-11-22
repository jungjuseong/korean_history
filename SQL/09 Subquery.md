
# Chapter 9. Subqueries

Subqueries are a powerful tool that you can use in all four SQL data statements. In this chapter, I’ll show you how subqueries can be used to filter data, generate values, and construct temporary data sets. After a little experimentation, I think you’ll agree that subqueries are one of the most powerful features of the SQL language.

## What Is a Subquery?

A subquery is a query contained within another SQL statement (which I refer to as the containing statement for the rest of this discussion). A subquery is always enclosed within parentheses, and it is usually executed prior to the containing statement. Like any query, a subquery returns a result set that may consist of:

- A single row with a single column

- Multiple rows with a single column

- Multiple rows having multiple columns

The type of result set returned by the subquery determines how it may be used and which operators the containing statement may use to interact with the data the subquery returns. When the containing statement has finished executing, the data returned by any subqueries is discarded, making a subquery act like a temporary table with statement scope (meaning that the server frees up any memory allocated to the subquery results after the SQL statement has finished execution).

You already saw several examples of subqueries in earlier chapters, but here’s a simple example to get started:

```sql
SELECT customer_id, first_name, last_name
    FROM customer
    WHERE customer_id = (SELECT MAX(customer_id) FROM customer\);
```
```
+-------------+------------+-----------+
| customer_id | first_name | last_name |
+-------------+------------+-----------+
|         599 | AUSTIN     | CINTRON   |
+-------------+------------+-----------+
1 row in set (0.27 sec)
```

In this example, the subquery returns the maximum value found in the customer_id column in the customer table, and the containing statement then returns data about that customer. If you are ever confused about what a subquery is doing, you can run the subquery by itself (without the parentheses) to see what it returns. Here’s the subquery from the previous example:

```sql
SELECT MAX(customer_id) FROM customer;
```
```
+------------------+
| MAX(customer_id) |
+------------------+
|              599 |
+------------------+
1 row in set (0.00 sec)
```

서브 쿼리는 단일 열이 있는 단일 행을 반환하므로 같음 조건에서 표현식 중 하나로 사용할 수 있습니다(서브쿼리가 두 개 이상의 행을 반환하면 무언가와 비교할 수 있지만 아무것도 같지 않을 수 있습니다. 그러나 이에 대한 자세한 내용은 나중에). 이 경우 서브 쿼리에서 반환된 값을 다음과 같이 포함하는 쿼리에서 필터 조건의 오른쪽 표현식으로 대체할 수 있습니다.

```sql
SELECT customer_id, first_name, last_name
    FROM customer
    WHERE customer_id = 599;
+-------------+------------+-----------+
| customer_id | first_name | last_name |
+-------------+------------+-----------+
|         599 | AUSTIN     | CINTRON   |
+-------------+------------+-----------+
1 row in set (0.00 sec)
```

서브 쿼리는 하나의 쿼리를 사용하여 최대 customer_id를 검색한 다음 두 번째 쿼리를 작성하여 고객 테이블에서 원하는 데이터를 검색하는 대신 단일 쿼리에서 가장 높은 ID를 가진 고객에 대한 정보를 검색할 수 있으므로 이 경우에 유용합니다. 서브 쿼리는 다른 많은 상황에서도 유용하며 SQL 툴킷에서 가장 강력한 도구 중 하나가 될 수 있습니다.

## 서브쿼리 유형

서브쿼리에서 반환된 결과 집합 유형(단일 행/열, 단일 행/다중 열 또는 여러 열)과 관련하여 앞에서 언급한 차이점과 함께 다른 기능을 사용하여 서브 쿼리를 구별할 수 있습니다. 일부 서브쿼리는 완전히 독립적이며(비상관 서브 쿼리라고 함), 다른 서브쿼리는 포함 명령문의 열을 참조합니다(상관 서브 쿼리라고 함). 여러 섹션에서는 이러한 두 가지 서브쿼리 유형을 살펴보고 이들과 상호 작용하기 위해 사용할 수 있는 다양한 연산자를 보여줍니다.

### 비상관 서브쿼리
이 장의 앞부분에 있는 예제는 비상관 서브 쿼리입니다. 단독으로 실행될 수 있으며 포함하는 명령문에서 아무 것도 참조하지 않습니다. 업데이트 또는 삭제 문을 작성하지 않는 한, 마주치는 대부분의 서브 쿼리가 이 유형이 될 것입니다. 

이러한 유형은 상관 서브쿼리를 자주 사용합니다(자세한 내용은 나중에 설명). 비상관 서브쿼리와 함께 이 장의 앞부분에 있는 예제는 단일 행과 열을 포함하는 결과 집합도 반환합니다. 이러한 유형의 서브쿼리는 스칼라 서브 쿼리로 알려져 있으며 일반적인 연산자(=, <>, <, >, <=, >=)를 사용하여 조건의 양쪽에 나타날 수 있습니다. 다음 예는 부등식 조건에서 스칼라 서브 쿼리를 사용하는 방법을 보여줍니다.

```sql
SELECT city_id, city
    FROM city
    WHERE country_id <> 
     (SELECT country_id FROM country WHERE country = 'India'\);
```
```

+---------+----------------------------+
| city_id | city                       |
+---------+----------------------------+
|       1 | A Corua (La Corua)         |
|       2 | Abha                       |
|       3 | Abu Dhabi                  |
|       4 | Acua                       |
|       5 | Adana                      |
|       6 | Addis Abeba                |
...
|     595 | Zapopan                    |
|     596 | Zaria                      |
|     597 | Zeleznogorsk               |
|     598 | Zhezqazghan                |
|     599 | Zhoushan                   |
|     600 | Ziguinchor                 |
+---------+----------------------------+
540 rows in set (0.02 sec)
```

이 쿼리는 인도에 없는 모든 도시를 반환합니다. 문의 마지막 줄에 있는 서브쿼리는 인도의 국가 ID를 반환하고 포함 쿼리는 해당 국가 ID가 없는 모든 도시를 반환합니다. 이 예제의 서브쿼리는 매우 간단하지만 서브 쿼리는 필요한 만큼 복잡할 수 있으며 사용 가능한 모든 쿼리 절(select, from, where, group by, have, order by)을 활용할 수 있습니다.

같음 조건에서 서브 쿼리를 사용하지만 서브 쿼리가 둘 이상의 행을 반환하면 오류가 발생합니다. 예를 들어 쿼리가 인도를 제외한 모든 국가를 반환하도록 이전 쿼리를 수정하면 다음 오류가 발생합니다.

```sql
SELECT city_id, city
    FROM city
    WHERE country_id <> 
     (SELECT country_id FROM country WHERE country <> 'India'\);
```
```
ERROR 1242 (21000): Subquery returns more than 1 row
```

If you run the subquery by itself, you will see the following results:

```sql
SELECT country_id FROM country WHERE country <> 'India';
```
```
+------------+
| country_id |
+------------+
|          1 |
|          2 |
|          3 |
|          4 |
...
|        106 |
|        107 |
|        108 |
|        109 |
+------------+
108 rows in set (0.00 sec)
```

표현식(country_id)을 표현식 세트(country_ids 1, 2, 3, ..., 109)와 동일시할 수 없기 때문에 포함하는 쿼리가 실패합니다. 다시 말해서, 하나의 사물은 사물의 집합과 동일시될 수 없습니다. 다음 섹션에서는 다른 연산자를 사용하여 문제를 해결하는 방법을 볼 것입니다.

## Multiple-Row, Single-Column Subqueries

서브 쿼리가 둘 이상의 행을 반환하는 경우 이전 예제에서 설명한 것처럼 같음 조건의 한쪽에서 사용할 수 없습니다. 그러나 이러한 유형의 쿼리로 조건을 작성하는 데 사용할 수 있는 네 가지 연산자가 있습니다. 서브 쿼리가 둘 이상의 행을 반환하는 경우 이전 예제에서 설명한 것처럼 같음 조건의 한쪽에서 사용할 수 없습니다. 그러나 이러한 유형의 쿼리로 조건을 작성하는 데 사용할 수 있는 네 가지 연산자가 있습니다.

### in and not in 연산자

While you can’t equate a single value to a set of values, you can check to see whether a single value can be found within a set of values. The next example, while it doesn’t use a subquery, demonstrates how to build a condition that uses the in operator to search for a value within a set of values:

```sql
SELECT country_id
    FROM country
    WHERE country IN ('Canada','Mexico'\);
```
```

+------------+
| country_id |
+------------+
|         20 |
|         60 |
+------------+
2 rows in set (0.00 sec)
```

The expression on the lefthand side of the condition is the country column, while the righthand side of the condition is a set of strings. The in operator checks to see whether either of the strings can be found in the country column; if so, the condition is met, and the row is added to the result set. You could achieve the same results using two equality conditions, as in:

```sql
SELECT country_id
    FROM country
    WHERE country = 'Canada' OR country = 'Mexico';
+------------+
| country_id |
+------------+
|         20 |
|         60 |
+------------+
2 rows in set (0.00 sec)
```

이 접근 방식은 집합에 두 개의 표현식만 포함되어 있을 때 합리적으로 보이지만 집합에 수십(또는 수백, 수천 등) 값이 포함된 경우 in 연산자를 사용하는 단일 조건이 선호되는 이유를 쉽게 알 수 있습니다.

때때로 조건의 한쪽에 사용할 문자열, 날짜 또는 숫자 집합을 생성하지만 하나 이상의 행을 반환하는 서브쿼리를 사용하여 집합을 생성할 가능성이 더 큽니다. 다음 쿼리는 필터 조건의 오른쪽에 있는 서브쿼리와 함께 in 연산자를 사용하여 캐나다 또는 멕시코에 있는 모든 도시를 반환합니다. 이 접근 방식은 집합에 두 개의 표현식만 포함되어 있을 때 합리적으로 보이지만 단일 표현식이 포함된 이유를 쉽게 알 수 있습니다. 집합에 수십(또는 수백, 수천 등) 값이 포함된 경우 in 연산자를 사용하는 조건이 더 좋습니다.

```sql
SELECT city_id, city
    FROM city
    WHERE country_id IN
     (SELECT country_id
      FROM country
      WHERE country IN ('Canada','Mexico')\);
```
```
+---------+----------------------------+
| city_id | city                       |
+---------+----------------------------+
|     179 | Gatineau                   |
|     196 | Halifax                    |
|     300 | Lethbridge                 |
|     313 | London                     |
|     383 | Oshawa                     |
|     430 | Richmond Hill              |
|     565 | Vancouver                  |
...
|     452 | San Juan Bautista Tuxtepec |
|     541 | Torren                     |
|     556 | Uruapan                    |
|     563 | Valle de Santiago          |
|     595 | Zapopan                    |
+---------+----------------------------+
37 rows in set (0.00 sec)
```

Along with seeing whether a value exists within a set of values, you can check the converse using the not in operator. Here’s another version of the previous query using not in instead of in:

```sql
SELECT city_id, city
    FROM city
    WHERE country_id NOT IN
     (SELECT country_id
      FROM country
      WHERE country IN ('Canada','Mexico')\);
```
```

+---------+----------------------------+
| city_id | city                       |
+---------+----------------------------+
|       1 | A Corua (La Corua)         |
|       2 | Abha                       |
|       3 | Abu Dhabi                  |
|       5 | Adana                      |
|       6 | Addis Abeba                |
...
|     596 | Zaria                      |
|     597 | Zeleznogorsk               |
|     598 | Zhezqazghan                |
|     599 | Zhoushan                   |
|     600 | Ziguinchor                 |
+---------+----------------------------+
563 rows in set (0.00 sec)
```

This query finds all cities that are not in Canada or Mexico.

### all 연산자

While the in operator is used to see whether an expression can be found within a set of expressions, the all operator allows you to make comparisons between a single value and every value in a set. To build such a condition, you will need to use one of the comparison operators (=, <>, <, >, etc.) in conjunction with the all operator. For example, the next query finds all customers who have never gotten a free film rental:

```sql
SELECT first_name, last_name
    FROM customer
    WHERE customer_id <> ALL
     (SELECT customer_id
      FROM payment
      WHERE amount = 0\);
```
```

+-------------+--------------+
| first_name  | last_name    |
+-------------+--------------+
| MARY        | SMITH        |
| PATRICIA    | JOHNSON      |
| LINDA       | WILLIAMS     |
| BARBARA     | JONES        |
...
| EDUARDO     | HIATT        |
| TERRENCE    | GUNDERSON    |
| ENRIQUE     | FORSYTHE     |
| FREDDIE     | DUGGAN       |
| WADE        | DELVALLE     |
| AUSTIN      | CINTRON      |
+-------------+--------------+
576 rows in set (0.01 sec)
```

서브쿼리는 영화 대여에 대해 0달러를 지불한 고객의 ID 집합을 반환하고 포함 쿼리는 서브쿼리에서 반환된 집합에 ID가 없는 모든 고객의 이름을 반환합니다. 이 접근 방식이 약간 서투르게 보인다면 좋은 회사에 있는 것입니다. 대부분의 사람들은 쿼리를 다르게 표현하고 all 연산자를 사용하지 않는 것을 선호합니다. 설명을 위해 이전 쿼리는 not in 연산자를 사용하는 다음 예제와 동일한 결과를 생성합니다.

```sql
SELECT first_name, last_name
FROM customer
WHERE customer_id NOT IN
 (SELECT customer_id
  FROM payment
  WHERE amount = 0)
```
It’s a matter of preference, but I think that most people would find the version that uses not in to be easier to understand.

---
NOTE

When using not in or <> all to compare a value to a set of values, you must be careful to ensure that the set of values does not contain a null value, because the server equates the value on the lefthand side of the expression to each member of the set, and any attempt to equate a value to null yields unknown. Thus, the following query returns an empty set:

---

```sql
SELECT first_name, last_name
    FROM customer
    WHERE customer_id NOT IN (122, 452, NULL\);
```
```
Empty set (0.00 sec)
```

Here’s another example using the all operator, but this time the subquery is in the  having clause:

```sql
SELECT customer_id, count(*)
    FROM rental
    GROUP BY customer_id
    HAVING count(*) > ALL
     (SELECT count(*)
      FROM rental r
        INNER JOIN customer c
        ON r.customer_id = c.customer_id
        INNER JOIN address a
        ON c.address_id = a.address_id
        INNER JOIN city ct
        ON a.city_id = ct.city_id
        INNER JOIN country co
        ON ct.country_id = co.country_id
      WHERE co.country IN ('United States','Mexico','Canada')
      GROUP BY r.customer_id
    );
```
```

+-------------+----------+
| customer_id | count(*) |
+-------------+----------+
|         148 |       46 |
+-------------+----------+
1 row in set (0.01 sec)
```

The subquery in this example returns the total number of film rentals for all customers in North America, and the containing query returns all customers whose total number of film rentals exceeds any of the North American customers.

### any operator

Like the all operator, the any operator allows a value to be compared to the members of a set of values; unlike all, however, a condition using the any operator evaluates to true as soon as a single comparison is favorable. This example will find all customers whose total film rental payments exceed the total payments for all customers in Bolivia, Paraguay, or Chile:

```sql
SELECT customer_id, sum(amount)
    FROM payment
    GROUP BY customer_id
    HAVING sum(amount) > ANY
     (SELECT sum(p.amount)
      FROM payment p
        INNER JOIN customer c
        ON p.customer_id = c.customer_id
        INNER JOIN address a
        ON c.address_id = a.address_id
        INNER JOIN city ct
        ON a.city_id = ct.city_id
        INNER JOIN country co
        ON ct.country_id = co.country_id
      WHERE co.country IN ('Bolivia','Paraguay','Chile')
      GROUP BY co.country
     \);
```
```

+-------------+-------------+
| customer_id | sum(amount) |
+-------------+-------------+
|         137 |      194.61 |
|         144 |      195.58 |
|         148 |      216.54 |
|         178 |      194.61 |
|         459 |      186.62 |
|         526 |      221.55 |
+-------------+-------------+
6 rows in set (0.03 sec)
```

서브쿼리는 볼리비아, 파라과이 및 칠레의 모든 고객에 대한 총 영화 대여료를 반환하고 포함 쿼리는 이 세 국가 중 하나 이상을 지출한 모든 고객을 반환합니다. Netflix 구독 및 볼리비아, 파라과이 또는 칠레 여행을 예약하세요...).

---
노트

대부분의 사람들이 in을 사용하는 것을 선호하지만 = any를 사용하는 것은 in 연산자를 사용하는 것과 같습니다.

---

### 다중 열 서브쿼리

지금까지 이 장의 서브쿼리 예제에서는 단일 열과 하나 이상의 행을 반환했습니다. 그러나 특정 상황에서는 두 개 이상의 열을 반환하는 서브쿼리를 사용할 수 있습니다. 다중 열 서브쿼리의 유용성을 보여주기 위해 먼저 여러 개의 단일 열 서브쿼리를 사용하는 예를 살펴보는 것이 도움이 될 수 있습니다.

```sql
SELECT fa.actor_id, fa.film_id
    FROM film_actor fa
    WHERE fa.actor_id IN
     (SELECT actor_id FROM actor WHERE last_name = 'MONROE')
      AND fa.film_id IN
     (SELECT film_id FROM film WHERE rating = 'PG'\);
```
```
+----------+---------+
| actor_id | film_id |
+----------+---------+
|      120 |      63 |
|      120 |     144 |
|      120 |     414 |
|      120 |     590 |
|      120 |     715 |
|      120 |     894 |
|      178 |     164 |
|      178 |     194 |
|      178 |     273 |
|      178 |     311 |
|      178 |     983 |
+----------+---------+
11 rows in set (0.00 sec)
```

이 쿼리는 두 개의 서브쿼리를 사용하여 성이 Monroe인 모든 배우와 PG 등급의 모든 영화를 식별하고 포함 쿼리는 이 정보를 사용하여 Monroe라는 배우가 PG 영화에 등장한 모든 경우를 검색합니다. 그러나 두 개의 단일 열 서브쿼리를 하나의 다중 열 서브쿼리로 병합하고 그 결과를 film_actor 테이블의 두 열과 비교할 수 있습니다. 그렇게 하려면 필터 조건이 다음과 같이 서브쿼리에서 반환된 것과 같은 순서로 괄호로 묶인 film_actor 테이블의 두 열 이름을 지정해야 합니다.

```sql
SELECT actor_id, film_id
    FROM film_actor
    WHERE (actor_id, film_id) IN
     (SELECT a.actor_id, f.film_id
      FROM actor a
         CROSS JOIN film f
      WHERE a.last_name = 'MONROE'
      AND f.rating = 'PG'\);
```
```
+----------+---------+
| actor_id | film_id |
+----------+---------+
|      120 |      63 |
|      120 |     144 |
|      120 |     414 |
|      120 |     590 |
|      120 |     715 |
|      120 |     894 |
|      178 |     164 |
|      178 |     194 |
|      178 |     273 |
|      178 |     311 |
|      178 |     983 |
+----------+---------+
11 rows in set (0.00 sec)
```

이 버전의 쿼리는 이전 예와 동일한 기능을 수행하지만 각각 단일 열을 반환하는 두 개의 서브쿼리 대신 두 개의 열을 반환하는 단일 서브쿼리를 사용합니다. 이 버전의 서브쿼리는 교차 조인이라는 조인 유형을 사용합니다. 이에 대해서는 다음 장에서 살펴보겠습니다. 기본 아이디어는 Monroe(2)라는 배우의 모든 조합과 PG(194) 등급의 모든 영화를 총 388행으로 반환하는 것입니다. 그 중 11개 행은 film_actor 테이블에서 찾을 수 있습니다.

### 상관 서브쿼리

지금까지 표시된 모든 서브쿼리는 포함하는 명령문과 독립적입니다. 즉, 스스로 실행하고 결과를 검사할 수 있습니다. 반면에 상관 서브쿼리는 하나 이상의 열을 참조하는 포함 명령문에 종속됩니다. 비상관 서브쿼리와 달리 상관 서브쿼리는 포함 명령문을 실행하기 전에 한 번도 실행되지 않습니다.

대신, 상관된 서브쿼리는 각 후보 행(최종 결과에 포함될 수 있는 행)에 대해 한 번 실행됩니다. 예를 들어 다음 쿼리는 상관 서브쿼리를 사용하여 각 고객의 영화 대여 횟수를 계산하고 포함 쿼리는 정확히 20편의 영화를 대여한 고객을 검색합니다.

```sql
SELECT c.first_name, c.last_name
    FROM customer c
    WHERE 20 =
     (SELECT count(*) FROM rental r
      WHERE r.customer_id = c.customer_id\);
```
```
+------------+-------------+
| first_name | last_name   |
+------------+-------------+
| LAUREN     | HUDSON      |
| JEANETTE   | GREENE      |
| TARA       | RYAN        |
| WILMA      | RICHARDS    |
| JO         | FOWLER      |
| KAY        | CALDWELL    |
| DANIEL     | CABRAL      |
| ANTHONY    | SCHWAB      |
| TERRY      | GRISSOM     |
| LUIS       | YANEZ       |
| HERBERT    | KRUGER      |
| OSCAR      | AQUINO      |
| RAUL       | FORTIER     |
| NELSON     | CHRISTENSON |
| ALFREDO    | MCADAMS     |
+------------+-------------+
15 rows in set (0.01 sec)
```

서브쿼리의 맨 끝에 있는 c.customer_id에 대한 참조는 서브쿼리를 상관 관계로 만드는 것입니다. 포함하는 쿼리는 실행할 서브쿼리에 대해 c.customer_id에 대한 값을 제공해야 합니다. 이 경우 포함 쿼리는 고객 테이블에서 모든 599개 행을 검색하고 각 고객에 대해 서브쿼리를 한 번씩 실행하여 각 실행에 적절한 고객 ID를 전달합니다. 서브쿼리가 값 20을 반환하면 필터 조건이 충족되고 행이 결과 집합에 추가됩니다.

---
노트

상관 서브쿼리는 포함하는 쿼리의 각 행에 대해 한 번씩 실행되므로 포함된 쿼리가 많은 수의 행을 반환하는 경우 상관된 서브쿼리를 사용하면 성능 문제가 발생할 수 있습니다.

---

같음 조건과 함께 여기에 설명된 범위 조건과 같은 다른 유형의 조건에서 상관된 서브쿼리를 사용할 수 있습니다.

```sql
SELECT c.first_name, c.last_name
    FROM customer c
    WHERE
     (SELECT sum(p.amount) FROM payment p
      WHERE p.customer_id = c.customer_id)
      BETWEEN 180 AND 240;
+------------+-----------+
| first_name | last_name |
+------------+-----------+
| RHONDA     | KENNEDY   |
| CLARA      | SHAW      |
| ELEANOR    | HUNT      |
| MARION     | SNYDER    |
| TOMMY      | COLLAZO   |
| KARL       | SEAL      |
+------------+-----------+
6 rows in set (0.03 sec)
```

This variation on the previous query finds all customers whose total payments for all film rentals are between $180 and $240. Once again, the correlated subquery is executed 599 times (once for each customer row), and each execution of the subquery returns the total account balance for the given customer.

---

NOTE

Another subtle difference in the previous query is that the subquery is on the lefthand side of the condition, which may look a bit odd but is perfectly valid.

---

### exists Operator

While you will often see correlated subqueries used in equality and range conditions, the most common operator used to build conditions that utilize correlated subqueries is the exists operator. You use the exists operator when you want to identify that a relationship exists without regard for the quantity; for example, the following query finds all the customers who rented at least one film prior to May 25, 2005, without regard for how many films were rented:

```sql
SELECT c.first_name, c.last_name
    FROM customer c
    WHERE EXISTS
     (SELECT 1 FROM rental r
      WHERE r.customer_id = c.customer_id
        AND date(r.rental_date) < '2005-05-25'\);
```
```
+------------+-------------+
| first_name | last_name   |
+------------+-------------+
| CHARLOTTE  | HUNTER      |
| DELORES    | HANSEN      |
| MINNIE     | ROMERO      |
| CASSANDRA  | WALTERS     |
| ANDREW     | PURDY       |
| MANUEL     | MURRELL     |
| TOMMY      | COLLAZO     |
| NELSON     | CHRISTENSON |
+------------+-------------+
8 rows in set (0.03 sec)
```

Using the exists operator, your subquery can return zero, one, or many rows, and the condition simply checks whether the subquery returned one or more rows. If you look at the select clause of the subquery, you will see that it consists of a single literal (1); since the condition in the containing query only needs to know how many rows have been returned, the actual data the subquery returned is irrelevant. Your subquery can return whatever strikes your fancy, as demonstrated next:

```sql
SELECT c.first_name, c.last_name
    FROM customer c
    WHERE EXISTS
     (SELECT r.rental_date, r.customer_id, 'ABCD' str, 2 * 3 / 7 nmbr
      FROM rental r
      WHERE r.customer_id = c.customer_id
        AND date(r.rental_date) < '2005-05-25'\);
```
```

+------------+-------------+
| first_name | last_name   |
+------------+-------------+
| CHARLOTTE  | HUNTER      |
| DELORES    | HANSEN      |
| MINNIE     | ROMERO      |
| CASSANDRA  | WALTERS     |
| ANDREW     | PURDY       |
| MANUEL     | MURRELL     |
| TOMMY      | COLLAZO     |
| NELSON     | CHRISTENSON |
+------------+-------------+
8 rows in set (0.03 sec)
```

However, the convention is to specify either select 1 or select * when using exists.

You may also use not exists to check for subqueries that return no rows, as demonstrated by the following:

```sql
SELECT a.first_name, a.last_name
    FROM actor a
    WHERE NOT EXISTS
     (SELECT 1
      FROM film_actor fa
        INNER JOIN film f ON f.film_id = fa.film_id
      WHERE fa.actor_id = a.actor_id
        AND f.rating = 'R'\);
```
```

+------------+-----------+
| first_name | last_name |
+------------+-----------+
| JANE       | JACKMAN   |
+------------+-----------+
1 row in set (0.00 sec)
```

This query finds all actors who have never appeared in an R-rated film.


## 상관 서브쿼리를 사용한 데이터 조작

지금까지 이 장의 모든 예제는 select 문이지만 다른 SQL 문에서 서브쿼리가 유용하지 않다는 것을 의미하지는 않습니다. 서브쿼리는 업데이트, 삭제 및 삽입 문에서도 많이 사용되며 관련 서브쿼리는 업데이트 및 삭제 문에서 자주 나타납니다. 다음은 고객 테이블의 last_update 열을 수정하는 데 사용되는 상관 서브쿼리의 예입니다.

```sql
UPDATE customer c
SET c.last_update =
 (SELECT max(r.rental_date) FROM rental r
  WHERE r.customer_id = c.customer_id\);
```

이 문은 rent 테이블에서 각 고객의 최신 임대 날짜를 찾아 고객 테이블의 모든 행을 수정합니다(where 절이 없기 때문에). 모든 고객이 최소한 한 번은 영화를 대여할 것으로 예상하는 것이 합리적으로 보이지만 last_update 열을 업데이트하기 전에 확인하는 것이 가장 좋습니다. 그렇지 않으면 서브쿼리가 행을 반환하지 않으므로 열이 null로 설정됩니다. 다음은 두 번째 상관 서브쿼리가 있는 where 절을 사용하는 업데이트 문의 다른 버전입니다.

```sql
UPDATE customer c
SET c.last_update =
 (SELECT max(r.rental_date) FROM rental r
  WHERE r.customer_id = c.customer_id)
WHERE EXISTS
 (SELECT 1 FROM rental r
  WHERE r.customer_id = c.customer_id\);
```

두 개의 상관된 서브쿼리는 선택 절을 제외하고 동일합니다. 그러나 set 절의 서브쿼리는 update 문의 where 절의 조건이 true로 평가되는 경우(고객에 대해 하나 이상의 임대가 발견되었음을 의미함)에만 실행되므로 last_update 열의 데이터가 다음으로 덮어쓰여지지 않도록 보호합니다. 없는.

상관 서브쿼리는 삭제 문에서도 일반적입니다. 예를 들어 매월 말에 불필요한 데이터를 제거하는 데이터 유지 관리 스크립트를 실행할 수 있습니다. 스크립트에는 지난 1년 동안 영화 대여가 없었던 고객 테이블에서 행을 제거하는 다음 명령문이 포함될 수 있습니다.

```sql
DELETE FROM customer
WHERE 365 < ALL
 (SELECT datediff(now(), r.rental_date) days_since_last_rental
  FROM rental r
  WHERE r.customer_id = customer.customer_id\);
```

When using correlated subqueries with delete statements in MySQL, keep in mind that, for whatever reason, table aliases are not allowed when using delete, which is why I had to use the entire table name in the subquery. With most other database servers, you could provide an alias for the customer table, such as:

```sql
DELETE FROM customer c
WHERE 365 < ALL
 (SELECT datediff(now(), r.rental_date) days_since_last_rental
  FROM rental r
  WHERE r.customer_id = c.customer_id\);
```


## 서브쿼리를 사용하는 경우

다양한 유형의 서브쿼리와 서브쿼리에서 반환된 데이터와 상호 작용하기 위해 사용할 수 있는 다양한 연산자에 대해 배웠으므로 이제 서브쿼리를 사용하여 강력한 SQL 문을 작성할 수 있는 다양한 방법을 탐색할 차례입니다. 다음 세 섹션에서는 서브쿼리를 사용하여 사용자 지정 테이블을 구성하고 조건을 만들고 결과 집합에서 열 값을 생성하는 방법을 보여줍니다.

### 데이터 소스로서의 서브쿼리
3장으로 돌아가서 select 문의 from 절에는 쿼리에서 사용할 테이블이 포함되어 있다고 언급했습니다. 서브쿼리는 데이터의 행과 열을 포함하는 결과 집합을 생성하므로 테이블과 함께 from 절에 서브쿼리를 포함하는 것이 완벽하게 유효합니다. 언뜻 보기에는 실질적인 이점이 없는 흥미로운 기능처럼 보일 수 있지만 테이블과 함께 서브쿼리를 사용하는 것은 쿼리를 작성할 때 사용할 수 있는 가장 강력한 도구 중 하나입니다. 다음은 간단한 예입니다.

```sql
SELECT c.first_name, c.last_name, 
      pymnt.num_rentals, pymnt.tot_payments
    FROM customer c
      INNER JOIN
       (SELECT customer_id, 
          count(*) num_rentals, sum(amount) tot_payments
        FROM payment
        GROUP BY customer_id
       ) pymnt
      ON c.customer_id = pymnt.customer_id;
```
```
+-------------+--------------+-------------+--------------+
| first_name  | last_name    | num_rentals | tot_payments |
+-------------+--------------+-------------+--------------+
| MARY        | SMITH        |          32 |       118.68 |
| PATRICIA    | JOHNSON      |          27 |       128.73 |
| LINDA       | WILLIAMS     |          26 |       135.74 |
| BARBARA     | JONES        |          22 |        81.78 |
| ELIZABETH   | BROWN        |          38 |       144.62 |
...
| TERRENCE    | GUNDERSON    |          30 |       117.70 |
| ENRIQUE     | FORSYTHE     |          28 |        96.72 |
| FREDDIE     | DUGGAN       |          25 |        99.75 |
| WADE        | DELVALLE     |          22 |        83.78 |
| AUSTIN      | CINTRON      |          19 |        83.81 |
+-------------+--------------+-------------+--------------+
599 rows in set (0.03 sec)
```

In this example, a subquery generates a list of customer IDs along with the number of film rentals and the total payments. Here’s the result set generated by the subquery:

```sql
SELECT customer_id, count(*) num_rentals, sum(amount) tot_payments
    FROM payment
    GROUP BY customer_id;
```
```
+-------------+-------------+--------------+
| customer_id | num_rentals | tot_payments |
+-------------+-------------+--------------+
|           1 |          32 |       118.68 |
|           2 |          27 |       128.73 |
|           3 |          26 |       135.74 |
|           4 |          22 |        81.78 |
...
|         596 |          28 |        96.72 |
|         597 |          25 |        99.75 |
|         598 |          22 |        83.78 |
|         599 |          19 |        83.81 |
+-------------+-------------+--------------+
599 rows in set (0.03 sec)
```

서브쿼리에는 pymnt라는 이름이 지정되고 customer_id 열을 통해 고객 테이블에 조인됩니다. 그런 다음 포함 쿼리는 pymnt 서브쿼리의 요약 열과 함께 고객 테이블에서 고객의 이름을 검색합니다.

from 절에 사용된 서브쿼리는 상관 관계가 없어야 합니다.1 서브쿼리가 먼저 실행되고 포함 쿼리가 실행을 마칠 때까지 데이터가 메모리에 유지됩니다. 서브쿼리는 쿼리를 작성할 때 엄청난 유연성을 제공합니다. 사용 가능한 테이블 집합을 훨씬 넘어 원하는 데이터의 거의 모든 보기를 생성한 다음 결과를 다른 테이블이나 서브쿼리에 결합할 수 있기 때문입니다. 보고서를 작성하거나 외부 시스템에 대한 데이터 피드를 생성하는 경우 수행하기 위해 여러 쿼리 또는 절차적 언어를 요구하는 데 사용되는 단일 쿼리로 작업을 수행할 수 있습니다.

### 데이터 조립

서브쿼리를 사용하여 기존 데이터를 요약하는 것과 함께 서브쿼리를 사용하여 데이터베이스 내에 어떤 형식으로도 존재하지 않는 데이터를 생성할 수 있습니다. 예를 들어, 영화 대여에 지출한 금액을 기준으로 고객을 그룹화할 수 있지만 데이터베이스에 저장되지 않은 그룹 정의를 사용하려고 할 수 있습니다. 예를 들어 고객을 표 9-1에 표시된 그룹으로 정렬한다고 가정해 보겠습니다.

Table 9-1. Customer payment groups

Group name	Lower limit	Upper limit
Small Fry

0

$74.99

Average Joes

$75

$149.99

Heavy Hitters

$150

$9,999,999.99

To generate these groups within a single query, you will need a way to define these three groups. The first step is to define a query that generates the group definitions:

```sql
SELECT 'Small Fry' name, 0 low_limit, 74.99 high_limit
    UNION ALL
    SELECT 'Average Joes' name, 75 low_limit, 149.99 high_limit
    UNION ALL
    SELECT 'Heavy Hitters' name, 150 low_limit, 9999999.99 high_limit;
```
```
+---------------+-----------+------------+
| name          | low_limit | high_limit |
+---------------+-----------+------------+
| Small Fry     |         0 |      74.99 |
| Average Joes  |        75 |     149.99 |
| Heavy Hitters |       150 | 9999999.99 |
+---------------+-----------+------------+
3 rows in set (0.00 sec)
```

세 가지 개별 쿼리의 결과를 단일 결과 집합으로 병합하기 위해 집합 연산자 공용체 all을 사용했습니다. 각 쿼리는 3개의 리터럴을 검색하고 3개의 쿼리 결과를 조합하여 3개의 행과 3개의 열이 있는 결과 집합을 생성합니다. 이제 원하는 그룹을 생성하기 위한 쿼리가 있으며 다른 쿼리의 from 절에 배치하여 고객 그룹을 생성할 수 있습니다.

```sql
SELECT pymnt_grps.name, count(*) num_customers
    FROM
     (SELECT customer_id,
        count(*) num_rentals, sum(amount) tot_payments
      FROM payment
      GROUP BY customer_id
     ) pymnt
      INNER JOIN
     (SELECT 'Small Fry' name, 0 low_limit, 74.99 high_limit
      UNION ALL
      SELECT 'Average Joes' name, 75 low_limit, 149.99 high_limit
      UNION ALL
      SELECT 'Heavy Hitters' name, 150 low_limit, 9999999.99 high_limit
     ) pymnt_grps
      ON pymnt.tot_payments
        BETWEEN pymnt_grps.low_limit AND pymnt_grps.high_limit
    GROUP BY pymnt_grps.name;
```
```
+---------------+---------------+
| name          | num_customers |
+---------------+---------------+
| Average Joes  |           515 |
| Heavy Hitters |            46 |
| Small Fry     |            38 |
+---------------+---------------+
3 rows in set (0.03 sec)
```

from 절에는 두 개의 서브쿼리가 있습니다. pymnt라는 이름의 첫 번째 서브쿼리는 각 고객에 대한 총 영화 대여 수와 총 지불액을 반환하고 pymnt_grps라는 두 번째 서브쿼리는 세 개의 고객 그룹을 생성합니다. 각 고객이 세 그룹 중 어느 그룹에 속해 있는지 찾아 두 개의 서브쿼리를 결합한 다음 각 그룹의 고객 수를 계산하기 위해 행을 그룹 이름으로 그룹화합니다.

물론 서브쿼리를 사용하는 대신 그룹 정의를 보관할 영구(또는 임시) 테이블을 구축하기로 결정할 수도 있습니다. 이 접근 방식을 사용하면 잠시 후 데이터베이스가 작은 특수 목적 테이블로 가득 차 있고 대부분이 생성된 이유를 기억하지 못할 것입니다. 그러나 서브쿼리를 사용하면 새 데이터를 저장해야 하는 분명한 비즈니스 요구가 있는 경우에만 데이터베이스에 테이블을 추가하는 정책을 따를 수 있습니다.

### 작업 지향 서브쿼리

각 고객의 이름, 도시, 총 대여 횟수 및 총 지불 금액을 보여주는 보고서를 생성한다고 가정해 보겠습니다. 지불, 고객, 주소 및 도시 테이블을 조인한 다음 고객의 이름과 성을 그룹화하여 이를 수행할 수 있습니다.

```sql
SELECT c.first_name, c.last_name, ct.city,
      sum(p.amount) tot_payments, count(*) tot_rentals
    FROM payment p
      INNER JOIN customer c
      ON p.customer_id = c.customer_id
      INNER JOIN address a
      ON c.address_id = a.address_id
      INNER JOIN city ct
      ON a.city_id = ct.city_id
    GROUP BY c.first_name, c.last_name, ct.city;
```
```
+-------------+------------+-----------------+--------------+-------------+
| first_name  | last_name  | city            | tot_payments | tot_rentals |
+-------------+------------+-----------------+--------------+-------------+
| MARY        | SMITH      | Sasebo          |       118.68 |          32 |
| PATRICIA    | JOHNSON    | San Bernardino  |       128.73 |          27 |
| LINDA       | WILLIAMS   | Athenai         |       135.74 |          26 |
| BARBARA     | JONES      | Myingyan        |        81.78 |          22 |
...
| TERRENCE    | GUNDERSON  | Jinzhou         |       117.70 |          30 |
| ENRIQUE     | FORSYTHE   | Patras          |        96.72 |          28 |
| FREDDIE     | DUGGAN     | Sullana         |        99.75 |          25 |
| WADE        | DELVALLE   | Lausanne        |        83.78 |          22 |
| AUSTIN      | CINTRON    | Tieli           |        83.81 |          19 |
+-------------+------------+-----------------+--------------+-------------+
599 rows in set (0.06 sec)
```

이 쿼리는 원하는 데이터를 반환하지만 쿼리를 자세히 살펴보면 고객, 주소 및 도시 테이블은 표시 목적으로만 필요하고 지불 테이블에는 그룹화(customer_id 및 amount ). 따라서 그룹을 생성하는 작업을 서브쿼리로 분리한 다음 다른 세 테이블을 서브쿼리로 생성된 테이블에 결합하여 원하는 최종 결과를 얻을 수 있습니다. 

다음은 그룹화 서브쿼리입니다.

```sql
SELECT customer_id,
      count(*) tot_rentals, sum(amount) tot_payments
    FROM payment
    GROUP BY customer_id;
```
```
+-------------+-------------+--------------+
| customer_id | tot_rentals | tot_payments |
+-------------+-------------+--------------+
|           1 |          32 |       118.68 |
|           2 |          27 |       128.73 |
|           3 |          26 |       135.74 |
|           4 |          22 |        81.78 |
...
|         595 |          30 |       117.70 |
|         596 |          28 |        96.72 |
|         597 |          25 |        99.75 |
|         598 |          22 |        83.78 |
|         599 |          19 |        83.81 |
+-------------+-------------+--------------+
599 rows in set (0.03 sec)
```

This is the heart of the query; the other tables are needed only to provide meaningful strings in place of the customer_id value. The next query joins the previous data set to the other three tables:

```sql
SELECT c.first_name, c.last_name,
      ct.city,
      pymnt.tot_payments, pymnt.tot_rentals
    FROM
     (SELECT customer_id,
        count(*) tot_rentals, sum(amount) tot_payments
      FROM payment
      GROUP BY customer_id
     ) pymnt
      INNER JOIN customer c
      ON pymnt.customer_id = c.customer_id
      INNER JOIN address a
      ON c.address_id = a.address_id
      INNER JOIN city ct
      ON a.city_id = ct.city_id;
```
```
+-------------+------------+-----------------+--------------+-------------+
| first_name  | last_name  | city            | tot_payments | tot_rentals |
+-------------+------------+-----------------+--------------+-------------+
| MARY        | SMITH      | Sasebo          |       118.68 |          32 |
| PATRICIA    | JOHNSON    | San Bernardino  |       128.73 |          27 |
| LINDA       | WILLIAMS   | Athenai         |       135.74 |          26 |
| BARBARA     | JONES      | Myingyan        |        81.78 |          22 |
...
| TERRENCE    | GUNDERSON  | Jinzhou         |       117.70 |          30 |
| ENRIQUE     | FORSYTHE   | Patras          |        96.72 |          28 |
| FREDDIE     | DUGGAN     | Sullana         |        99.75 |          25 |
| WADE        | DELVALLE   | Lausanne        |        83.78 |          22 |
| AUSTIN      | CINTRON    | Tieli           |        83.81 |          19 |
+-------------+------------+-----------------+--------------+-------------+
599 rows in set (0.06 sec)
```

아름다움은 보는 사람의 눈에 달려 있다는 것을 알고 있지만 이 버전의 쿼리가 크고 평평한 버전보다 훨씬 더 만족스럽습니다. 이 버전은 그룹화가 여러 개의 긴 문자열 열(customer.first_name, customer.last_name, city.city) 대신 단일 숫자 열(customer_id)에서 수행되기 때문에 더 빠르게 실행할 수도 있습니다.


## 일반적인 테이블 표현식

버전 8.0의 MySQL에 새로 추가된 공통 테이블 표현식(일명 CTE)은 꽤 오랫동안 다른 데이터베이스 서버에서 사용할 수 있었습니다. CTE는 쉼표로 구분된 여러 CTE를 포함할 수 있는 with 절의 쿼리 상단에 나타나는 명명된 서브쿼리입니다. 쿼리를 더 이해하기 쉽게 만드는 것과 함께 이 기능을 사용하면 각 CTE가 동일한 with 절에서 위에 정의된 다른 CTE를 참조할 수도 있습니다. 다음 예에는 세 개의 CTE가 포함되어 있습니다. 여기서 두 번째는 첫 번째를 나타내고 세 번째는 두 번째를 나타냅니다.

```sql
WITH actors_s AS
     (SELECT actor_id, first_name, last_name
      FROM actor
      WHERE last_name LIKE 'S%'
     ),
     actors_s_pg AS
     (SELECT s.actor_id, s.first_name, s.last_name,
        f.film_id, f.title
      FROM actors_s s
        INNER JOIN film_actor fa
        ON s.actor_id = fa.actor_id
        INNER JOIN film f
        ON f.film_id = fa.film_id
      WHERE f.rating = 'PG'
     ),
     actors_s_pg_revenue AS
     (SELECT spg.first_name, spg.last_name, p.amount
      FROM actors_s_pg spg
        INNER JOIN inventory i
        ON i.film_id = spg.film_id
        INNER JOIN rental r
        ON i.inventory_id = r.inventory_id
        INNER JOIN payment p
        ON r.rental_id = p.rental_id
     ) -- end of With clause
    SELECT spg_rev.first_name, spg_rev.last_name,
      sum(spg_rev.amount) tot_revenue
    FROM actors_s_pg_revenue spg_rev
    GROUP BY spg_rev.first_name, spg_rev.last_name
    ORDER BY 3 desc;
```
```
+------------+-------------+-------------+
| first_name | last_name   | tot_revenue |
+------------+-------------+-------------+
| NICK       | STALLONE    |      692.21 |
| JEFF       | SILVERSTONE |      652.35 |
| DAN        | STREEP      |      509.02 |
| GROUCHO    | SINATRA     |      457.97 |
| SISSY      | SOBIESKI    |      379.03 |
| JAYNE      | SILVERSTONE |      372.18 |
| CAMERON    | STREEP      |      361.00 |
| JOHN       | SUVARI      |      296.36 |
| JOE        | SWANK       |      177.52 |
+------------+-------------+-------------+
9 rows in set (0.18 sec)
```

이 쿼리는 캐스트에 성이 S로 시작하는 배우를 포함하는 PG 등급 영화 대여에서 생성된 총 수익을 계산합니다. 첫 번째 서브쿼리(actors_s)는 성이 S로 시작하는 모든 배우를 찾고 두 번째 서브쿼리(actors_s_pg)는 다음을 조인합니다. 영화 테이블에 데이터 세트와 PG 등급이 있는 영화에 대한 필터, 그리고 세 번째 서브쿼리(actors_s_pg_revenue)는 해당 데이터 세트를 지불 테이블에 결합하여 이러한 영화 중 하나를 대여하기 위해 지불한 금액을 검색합니다. 최종 쿼리는 단순히 이름/성을 기준으로 Actor_s_pg_revenue의 데이터를 그룹화하고 수익을 합산합니다.

---
**노트**

임시 테이블을 활용하여 후속 쿼리에 사용할 쿼리 결과를 저장하는 경향이 있는 사용자는 CTE를 매력적인 대안으로 생각할 수 있습니다.

---

### 표현식 생성기로서의 서브쿼리

이 장의 마지막 섹션에서는 단일 열, 단일 행 스칼라 서브쿼리를 사용하여 시작한 부분을 마칩니다. 필터 조건에서 사용되는 것과 함께 스칼라 서브쿼리는 쿼리의 select 및 order by 절과 insert 문의 values ​​절을 포함하여 표현식이 나타날 수 있는 모든 곳에서 사용할 수 있습니다.

"작업 지향 서브쿼리"에서 서브쿼리를 사용하여 나머지 쿼리에서 그룹화 메커니즘을 분리하는 방법을 보여주었습니다. 다음은 동일한 목적으로 서브쿼리를 사용하지만 다른 방식으로 사용하는 동일한 쿼리의 다른 버전입니다.

```sql
SELECT
     (SELECT c.first_name FROM customer c
      WHERE c.customer_id = p.customer_id
     ) first_name,
     (SELECT c.last_name FROM customer c
      WHERE c.customer_id = p.customer_id
     ) last_name,
     (SELECT ct.city
      FROM customer c
      INNER JOIN address a
        ON c.address_id = a.address_id
      INNER JOIN city ct
        ON a.city_id = ct.city_id
      WHERE c.customer_id = p.customer_id
     ) city,
      sum(p.amount) tot_payments,
      count(*) tot_rentals
    FROM payment p
    GROUP BY p.customer_id;
```
```
+-------------+------------+-----------------+--------------+-------------+
| first_name  | last_name  | city            | tot_payments | tot_rentals |
+-------------+------------+-----------------+--------------+-------------+
| MARY        | SMITH      | Sasebo          |       118.68 |          32 |
| PATRICIA    | JOHNSON    | San Bernardino  |       128.73 |          27 |
| LINDA       | WILLIAMS   | Athenai         |       135.74 |          26 |
| BARBARA     | JONES      | Myingyan        |        81.78 |          22 |
...
| TERRENCE    | GUNDERSON  | Jinzhou         |       117.70 |          30 |
| ENRIQUE     | FORSYTHE   | Patras          |        96.72 |          28 |
| FREDDIE     | DUGGAN     | Sullana         |        99.75 |          25 |
| WADE        | DELVALLE   | Lausanne        |        83.78 |          22 |
| AUSTIN      | CINTRON    | Tieli           |        83.81 |          19 |
+-------------+------------+-----------------+--------------+-------------+
599 rows in set (0.06 sec)
```

이 쿼리와 from 절에서 서브쿼리를 사용하는 이전 버전 사이에는 두 가지 주요 차이점이 있습니다.

- 고객, 주소 및 도시 테이블을 payment 데이터에 조인하는 대신 선택 절에서 상관된 스칼라 서브쿼리를 사용하여 고객의 이름/성 및 도시를 조회합니다.

- 고객 테이블은 한 번이 아니라 세 번(세 개의 서브쿼리 각각에서 한 번씩) 액세스됩니다.

- 스칼라 서브 쿼리는 하나의 열과 행만 반환할 수 있기 때문에 고객 테이블에 세 번 액세스하므로 고객과 관련된 세 개의 열이 필요한 경우 세 가지 다른 서브 쿼리를 사용해야 합니다.

이전에 언급했듯이 스칼라 서브쿼리는 order by 절에도 나타날 수 있습니다. 다음 쿼리는 배우의 이름과 성을 검색하고 배우가 출연한 영화 수를 기준으로 정렬합니다.

```sql
SELECT a.actor_id, a.first_name, a.last_name
    FROM actor a
    ORDER BY
     (SELECT count(*) FROM film_actor fa
      WHERE fa.actor_id = a.actor_id) DESC;
+----------+-------------+--------------+
| actor_id | first_name  | last_name    |
+----------+-------------+--------------+
|      107 | GINA        | DEGENERES    |
|      102 | WALTER      | TORN         |
|      198 | MARY        | KEITEL       |
|      181 | MATTHEW     | CARREY       |
...
|       71 | ADAM        | GRANT        |
|      186 | JULIA       | ZELLWEGER    |
|       35 | JUDY        | DEAN         |
|      199 | JULIA       | FAWCETT      |
|      148 | EMILY       | DEE          |
+----------+-------------+--------------+
200 rows in set (0.01 sec)
```

쿼리는 order by 절에서 상관된 스칼라 서브쿼리를 사용하여 영화 출연 횟수만 반환하며 이 값은 정렬 목적으로만 사용됩니다.

선택 문에서 상관된 스칼라 서브쿼리를 사용하는 것과 함께 비상관 스칼라 서브쿼리를 사용하여 삽입 문에 대한 값을 생성할 수 있습니다. 예를 들어, film_actor 테이블에 새 행을 생성하려고 하고 다음 데이터가 주어졌다고 가정해 보겠습니다.

- 배우의 이름과 성
- 영화의 이름

두 가지 방법을 선택할 수 있습니다. 두 개의 쿼리를 실행하여 영화와 배우에서 기본 키 값을 검색하고 해당 값을 insert 문에 넣거나 서브쿼리를 사용하여 insert 문 내에서 두 개의 키 값을 검색합니다. 다음은 후자의 접근 방식의 예입니다.

```sql
INSERT INTO film_actor (actor_id, film_id, last_update)
VALUES (
 (SELECT actor_id FROM actor
  WHERE first_name = 'JENNIFER' AND last_name = 'DAVIS'),
 (SELECT film_id FROM film
  WHERE title = 'ACE GOLDFINGER'),
 now()
 \);
```

단일 SQL 문을 사용하여 film_actor 테이블에 행을 생성하고 동시에 두 개의 외래 키 열 값을 조회할 수 있습니다.

## 서브쿼리 요약

- 이 장에서 많은 내용을 다루었으므로 복습하는 것이 좋습니다. 이 장의 예는 다음과 같은 서브쿼리를 보여줍니다.

- 단일 열과 행, 여러 행이 있는 단일 열, 여러 열과 행 반환

- 포함하는 명령문과 독립적입니다(상관되지 않은 서브쿼리).

- 포함하는 명령문에서 하나 이상의 열 참조(상관된 서브쿼리)

- 비교연산자와 특수목적연산자를 사용하는 조건에서 in, not in, exist, not exist

- select, update, delete, insert 문에서 찾을 수 있습니다.

- 쿼리에서 다른 테이블(또는 서브쿼리)에 조인될 수 있는 결과 집합 생성

- 값을 생성하여 테이블을 채우거나 쿼리 결과 집합의 열을 채우는 데 사용할 수 있습니다.

- 쿼리의 select, from, where, have, order by 절에 사용됩니다.

- 분명히, 서브쿼리는 매우 다재다능한 도구이므로 이 챕터를 처음 읽은 후 이러한 개념이 모두 빠져들지 않았다고 해서 기분이 나빠지지 마십시오. 서브쿼리의 다양한 용도를 계속 실험하다 보면 사소한 SQL 문을 작성할 때마다 서브쿼리를 어떻게 활용할 수 있을지 생각하게 될 것입니다.

## 지식 테스트

이 연습은 서브쿼리에 대한 이해도를 테스트하기 위해 고안되었습니다. 솔루션은 부록 B를 참조하십시오.

**연습 9-1**
모든 액션 영화(category.name = 'Action')를 찾기 위해 범주 테이블에 대해 상관되지 않은 서브쿼리가 있는 필터 조건을 사용하는 영화 테이블에 대한 쿼리를 구성합니다.

**연습 9-2**
동일한 결과를 얻기 위해 category 및 film_category 테이블에 대해 상관 서브쿼리를 사용하여 연습 9-1의 쿼리를 다시 작업합니다.

**연습 9-3**
다음 쿼리를 film_actor 테이블에 대한 서브쿼리에 결합하여 각 배우의 수준을 표시합니다.

```sql
SELECT 'Hollywood Star' level, 30 min_roles, 99999 max_roles
UNION ALL
SELECT 'Prolific Actor' level, 20 min_roles, 29 max_roles
UNION ALL
SELECT 'Newcomer' level, 1 min_roles, 19 max_roles
```

The subquery against the film_actor table should count the number of rows for each actor using group by actor_id, and the count should be compared to the min_roles/max_roles columns to determine which level each actor belongs to.