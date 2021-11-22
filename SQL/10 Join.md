
# 10장. 조인 재검토

지금쯤이면 5장에서 소개한 내부 조인의 개념에 익숙해질 것입니다. 이 장에서는 외부 조인과 교차 조인을 포함하여 테이블을 조인할 수 있는 다른 방법에 초점을 맞춥니다.

## 외부 조인

지금까지 여러 테이블이 포함된 모든 예제에서 조인 조건이 테이블의 모든 행에 대해 일치하는 항목을 찾지 못할 수도 있다는 점을 우려하지 않았습니다. 예를 들어 inventory 테이블에는 대여 가능한 모든 영화에 대한 행이 포함되어 있지만 movie 테이블에 있는 1,000개 행 중 958개만 inventory 테이블에 하나 이상의 행이 있습니다. 다른 42개의 영화는 대여할 수 없으므로(아마도 며칠 내로 도착할 신작임) 이러한 영화 ID는 inventory 테이블에서 찾을 수 없습니다. 

다음 쿼리는 이 두 테이블을 결합하여 각 필름의 사용 가능한 사본 수를 계산합니다.

```sql
SELECT f.film_id, f.title, count(*) num_copies
     FROM film f
       INNER JOIN inventory i
       ON f.film_id = i.film_id
     GROUP BY f.film_id, f.title;
```
```
+---------+-----------------------------+------------+
| film_id | title                       | num_copies |
+---------+-----------------------------+------------+
|       1 | ACADEMY DINOSAUR            |          8 |
|       2 | ACE GOLDFINGER              |          3 |
|       3 | ADAPTATION HOLES            |          4 |
|       4 | AFFAIR PREJUDICE            |          7 |
...
|      13 | ALI FOREVER                 |          4 |
|      15 | ALIEN CENTER                |          6 |
...
|     997 | YOUTH KICK                  |          2 |
|     998 | ZHIVAGO CORE                |          2 |
|     999 | ZOOLANDER FICTION           |          5 |
|    1000 | ZORRO ARK                   |          8 |
+---------+-----------------------------+------------+
958 rows in set (0.02 sec)
```
1,000개의 행이 반환될 것으로 예상했지만(각 영화에 대해 하나씩) 쿼리는 958개의 행만 반환합니다. 쿼리가 조인 조건을 충족하는 행만 반환하는 내부 조인을 사용하기 때문입니다. 예를 들어 영화 Alice Fantasia(film_id 14)는 inventory 테이블에 행이 없기 때문에 결과에 나타나지 않습니다.

inventory 테이블에 행이 있는지 여부에 관계없이 쿼리가 1,000편의 영화를 모두 반환하도록 하려면 기본적으로 조인 조건을 선택 사항으로 만드는 외부 조인을 사용할 수 있습니다.

```sql
SELECT f.film_id, f.title, count(i.inventory_id) num_copies
     FROM film f
       LEFT OUTER JOIN inventory i
       ON f.film_id = i.film_id
     GROUP BY f.film_id, f.title;
```
```
+---------+-----------------------------+------------+
| film_id | title                       | num_copies |
+---------+-----------------------------+------------+
|       1 | ACADEMY DINOSAUR            |          8 |
|       2 | ACE GOLDFINGER              |          3 |
|       3 | ADAPTATION HOLES            |          4 |
|       4 | AFFAIR PREJUDICE            |          7 |
...
|      13 | ALI FOREVER                 |          4 |
|      14 | ALICE FANTASIA              |          0 |
|      15 | ALIEN CENTER                |          6 |
...
|     997 | YOUTH KICK                  |          2 |
|     998 | ZHIVAGO CORE                |          2 |
|     999 | ZOOLANDER FICTION           |          5 |
|    1000 | ZORRO ARK                   |          8 |
+---------+-----------------------------+------------+
1000 rows in set (0.01 sec)
```
보시다시피 쿼리는 이제 영화 테이블에서 모든 1,000개 행을 반환하고 행 중 42개(Alice Fantasia 포함)의 num_copies 열 값은 0으로 인벤토리에 복사본이 없음을 나타냅니다.

다음은 이전 버전의 쿼리에서 변경된 사항에 대한 설명입니다.

조인 정의가 inner에서 left outer로 변경되어 서버가 조인의 왼쪽에 있는 테이블의 모든 행(이 경우 film)을 포함하고 조인의 오른쪽에 있는 테이블의 열을 포함하도록 지시합니다. (inventory) 

num_copies 열 정의가 count(*)에서 count(i.inventory_id)로 변경되었으며 이는 inventory.inventory_id 열의 null이 아닌 값의 수를 계산합니다.

다음으로, 내부 조인과 외부 조인의 차이점을 명확하게 보기 위해 group by 절을 제거하고 대부분의 행을 필터링하겠습니다. 다음은 내부 조인과 필터 조건을 사용하여 몇 편의 영화에 대한 행을 반환하는 쿼리입니다.

```sql
SELECT f.film_id, f.title, i.inventory_id
     FROM film f
       INNER JOIN inventory i
       ON f.film_id = i.film_id
     WHERE f.film_id BETWEEN 13 AND 15;
```
```
+---------+--------------+--------------+
| film_id | title        | inventory_id |
+---------+--------------+--------------+
|      13 | ALI FOREVER  |           67 |
|      13 | ALI FOREVER  |           68 |
|      13 | ALI FOREVER  |           69 |
|      13 | ALI FOREVER  |           70 |
|      15 | ALIEN CENTER |           71 |
|      15 | ALIEN CENTER |           72 |
|      15 | ALIEN CENTER |           73 |
|      15 | ALIEN CENTER |           74 |
|      15 | ALIEN CENTER |           75 |
|      15 | ALIEN CENTER |           76 |
+---------+--------------+--------------+
10 rows in set (0.00 sec)
```
The results show that there are four copies of Ali Forever and six copies of Alien Center in inventory. Here’s the same query, but using an outer join:

```sql
SELECT f.film_id, f.title, i.inventory_id
     FROM film f
       LEFT OUTER JOIN inventory i
       ON f.film_id = i.film_id
     WHERE f.film_id BETWEEN 13 AND 15;
```
```
+---------+----------------+--------------+
| film_id | title          | inventory_id |
+---------+----------------+--------------+
|      13 | ALI FOREVER    |           67 |
|      13 | ALI FOREVER    |           68 |
|      13 | ALI FOREVER    |           69 |
|      13 | ALI FOREVER    |           70 |
|      14 | ALICE FANTASIA |         NULL |
|      15 | ALIEN CENTER   |           71 |
|      15 | ALIEN CENTER   |           72 |
|      15 | ALIEN CENTER   |           73 |
|      15 | ALIEN CENTER   |           74 |
|      15 | ALIEN CENTER   |           75 |
|      15 | ALIEN CENTER   |           76 |
+---------+----------------+--------------+
11 rows in set (0.00 sec)
```
Ali Forever 및 Alien Center의 결과는 동일하지만 Alice Fantasia에 대한 하나의 새 행이 있으며 인벤토리.inventory_id 열에 null 값이 있습니다. 이 예는 외부 조인이 쿼리에서 반환되는 행 수를 제한하지 않고 열 값을 추가하는 방법을 보여줍니다. 조인 조건이 실패하면(Alice Fantasia의 경우와 같이) 외부 조인된 테이블에서 검색된 모든 열은 null이 됩니다.

### 왼쪽 대 오른쪽 외부 조인

이전 섹션의 각 외부 조인 예제에서 왼쪽 외부 조인을 지정했습니다. 키워드 left는 조인의 왼쪽에 있는 테이블이 결과 집합의 행 수를 결정하는 역할을 하는 반면, 오른쪽에 있는 테이블은 일치 항목이 발견될 때마다 열 값을 제공하는 데 사용됨을 나타냅니다. 

그러나 오른쪽 외부 조인을 지정할 수도 있습니다. 이 경우 조인의 오른쪽에 있는 테이블은 결과 집합의 행 수를 결정하는 반면 왼쪽에 있는 테이블은 열 값을 제공하는 데 사용됩니다.

다음은 왼쪽 외부 조인 대신 오른쪽 외부 조인을 사용하도록 재정렬된 이전 섹션의 마지막 쿼리입니다.

```sql
SELECT f.film_id, f.title, i.inventory_id
     FROM inventory i
       RIGHT OUTER JOIN film f
       ON f.film_id = i.film_id
     WHERE f.film_id BETWEEN 13 AND 15;
```
```
+---------+----------------+--------------+
| film_id | title          | inventory_id |
+---------+----------------+--------------+
|      13 | ALI FOREVER    |           67 |
|      13 | ALI FOREVER    |           68 |
|      13 | ALI FOREVER    |           69 |
|      13 | ALI FOREVER    |           70 |
|      14 | ALICE FANTASIA |         NULL |
|      15 | ALIEN CENTER   |           71 |
|      15 | ALIEN CENTER   |           72 |
|      15 | ALIEN CENTER   |           73 |
|      15 | ALIEN CENTER   |           74 |
|      15 | ALIEN CENTER   |           75 |
|      15 | ALIEN CENTER   |           76 |
+---------+----------------+--------------+
11 rows in set (0.00 sec)
```
두 버전의 쿼리가 모두 외부 조인을 수행하고 있음을 명심하십시오. 왼쪽 및 오른쪽 키워드는 데이터에 공백이 있는 테이블을 서버에 알려주기 위한 것입니다. 테이블 A와 B를 외부 조인하고 일치하는 데이터가 있을 때마다 B의 추가 열이 있는 A의 모든 행을 원하는 경우 A 왼쪽 외부 조인 B 또는 B 오른쪽 외부 조인 A를 지정할 수 있습니다.

---
노트

오른쪽 외부 조인이 거의 발생하지 않고 **모든 데이터베이스 서버가 이를 지원하는 것은 아니므로 항상 왼쪽 외부 조인을 사용하는 것이 좋습니다**. 외부 키워드는 선택 사항이므로 대신 A 왼쪽 조인 B를 선택할 수 있지만 명확성을 위해 외부를 포함하는 것이 좋습니다.

---

## 3방향 외부 조인

경우에 따라 한 테이블을 다른 두 테이블과 외부 조인할 수 있습니다. 예를 들어 이전 섹션의 쿼리를 확장하여 rent 테이블의 데이터를 포함할 수 있습니다.

```sql
SELECT f.film_id, f.title, i.inventory_id, r.rental_date
     FROM film f
       LEFT OUTER JOIN inventory i
       ON f.film_id = i.film_id
       LEFT OUTER JOIN rental r
       ON i.inventory_id = r.inventory_id
     WHERE f.film_id BETWEEN 13 AND 15;
```
```
+---------+----------------+--------------+---------------------+
| film_id | title          | inventory_id | rental_date         |
+---------+----------------+--------------+---------------------+
|      13 | ALI FOREVER    |           67 | 2005-07-31 18:11:17 |
|      13 | ALI FOREVER    |           67 | 2005-08-22 21:59:29 |
|      13 | ALI FOREVER    |           68 | 2005-07-28 15:26:20 |
|      13 | ALI FOREVER    |           68 | 2005-08-23 05:02:31 |
|      13 | ALI FOREVER    |           69 | 2005-08-01 23:36:10 |
|      13 | ALI FOREVER    |           69 | 2005-08-22 02:12:44 |
|      13 | ALI FOREVER    |           70 | 2005-07-12 10:51:09 |
|      13 | ALI FOREVER    |           70 | 2005-07-29 01:29:51 |
|      13 | ALI FOREVER    |           70 | 2006-02-14 15:16:03 |
|      14 | ALICE FANTASIA |         NULL | NULL                |
|      15 | ALIEN CENTER   |           71 | 2005-05-28 02:06:37 |
|      15 | ALIEN CENTER   |           71 | 2005-06-17 16:40:03 |
|      15 | ALIEN CENTER   |           71 | 2005-07-11 05:47:08 |
|      15 | ALIEN CENTER   |           71 | 2005-08-02 13:58:55 |
|      15 | ALIEN CENTER   |           71 | 2005-08-23 05:13:09 |
|      15 | ALIEN CENTER   |           72 | 2005-05-27 22:49:27 |
|      15 | ALIEN CENTER   |           72 | 2005-06-19 13:29:28 |
|      15 | ALIEN CENTER   |           72 | 2005-07-07 23:05:53 |
|      15 | ALIEN CENTER   |           72 | 2005-08-01 05:55:13 |
|      15 | ALIEN CENTER   |           72 | 2005-08-20 15:11:48 |
|      15 | ALIEN CENTER   |           73 | 2005-07-06 15:51:58 |
|      15 | ALIEN CENTER   |           73 | 2005-07-30 14:48:24 |
|      15 | ALIEN CENTER   |           73 | 2005-08-20 22:32:11 |
|      15 | ALIEN CENTER   |           74 | 2005-07-27 00:15:18 |
|      15 | ALIEN CENTER   |           74 | 2005-08-23 19:21:22 |
|      15 | ALIEN CENTER   |           75 | 2005-07-09 02:58:41 |
|      15 | ALIEN CENTER   |           75 | 2005-07-29 23:52:01 |
|      15 | ALIEN CENTER   |           75 | 2005-08-18 21:55:01 |
|      15 | ALIEN CENTER   |           76 | 2005-06-15 08:01:29 |
|      15 | ALIEN CENTER   |           76 | 2005-07-07 18:31:50 |
|      15 | ALIEN CENTER   |           76 | 2005-08-01 01:49:36 |
|      15 | ALIEN CENTER   |           76 | 2005-08-17 07:26:47 |
+---------+----------------+--------------+---------------------+
32 rows in set (0.01 sec)
```
결과에는 인벤토리에 있는 모든 영화의 모든 대여가 포함되지만 영화 Alice Fantasia는 외부 조인된 두 테이블의 열에 대해 null 값을 갖습니다.

### cross 조인

5장으로 돌아가서 카테지안 곱의 개념을 소개했는데, 이는 본질적으로 조인 조건을 지정하지 않고 여러 테이블을 조인한 결과입니다. 카테지안 곱은 우연히 꽤 자주 사용되지만(예: from 절에 조인 조건을 추가하는 것을 잊음) 그렇지 않으면 그렇게 일반적이지 않습니다. 그러나 두 테이블의 카테지안 곱을 생성하려는 경우 다음과 같이 교차 조인을 지정해야 합니다.

```sql
SELECT c.name category_name, l.name language_name
     FROM category c
       CROSS JOIN language l;
```
```
+---------------+---------------+
| category_name | language_name |
+---------------+---------------+
| Action        | English       |
| Action        | Italian       |
| Action        | Japanese      |
| Action        | Mandarin      |
| Action        | French        |
| Action        | German        |
| Animation     | English       |
| Animation     | Italian       |
| Animation     | Japanese      |
| Animation     | Mandarin      |
| Animation     | French        |
| Animation     | German        |
...
| Sports        | English       |
| Sports        | Italian       |
| Sports        | Japanese      |
| Sports        | Mandarin      |
| Sports        | French        |
| Sports        | German        |
| Travel        | English       |
| Travel        | Italian       |
| Travel        | Japanese      |
| Travel        | Mandarin      |
| Travel        | French        |
| Travel        | German        |
+---------------+---------------+
96 rows in set (0.00 sec)
```
이 쿼리는 범주 및 언어 테이블의 카테지안 곱을 생성하여 결과적으로 96행(범주 행 16개 × 언어 행 6개)이 생성됩니다. 그러나 이제 교차 조인이 무엇이며 지정하는 방법을 알았으니 무엇에 사용됩니까? 대부분의 SQL 책은 교차 조인이 무엇인지 설명한 다음 거의 유용하지 않다고 말하지만 교차 조인이 매우 유용하다고 생각하는 상황을 여러분과 공유하고 싶습니다.

9장에서 서브쿼리를 사용하여 테이블을 만드는 방법에 대해 설명했습니다. 내가 사용한 예는 다른 테이블에 조인될 수 있는 3행 테이블을 작성하는 방법을 보여주었습니다. 다음은 예제에서 만든 테이블입니다.

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
While this table was exactly what was needed for placing customers into three groups based on their total film payments, this strategy of merging single-row tables using the set operator union all doesn’t work very well if you need to fabricate a large table.

Say, for example, that you want to create a query that generates a row for every day in the year 2020 but you don’t have a table in your database that contains a row for every day. Using the strategy from the example in Chapter 9, you could do something like the following:

```sql
SELECT '2020-01-01' dt
UNION ALL
SELECT '2020-01-02' dt
UNION ALL
SELECT '2020-01-03' dt
UNION ALL
...
...
...
SELECT '2020-12-29' dt
UNION ALL
SELECT '2020-12-30' dt
UNION ALL
SELECT '2020-12-31' dt
```

Building a query that merges together the results of 366 queries is a bit tedious, so maybe a different strategy is needed. What if you generate a table with 366 rows (2020 is a leap year) with a single column containing a number between 0 and 366 and then add that number of days to January 1, 2020? Here’s one possible method to generate such a table:

```sql
SELECT ones.num + tens.num + hundreds.num
     FROM
     (SELECT 0 num UNION ALL
     SELECT 1 num UNION ALL
     SELECT 2 num UNION ALL
     SELECT 3 num UNION ALL
     SELECT 4 num UNION ALL
     SELECT 5 num UNION ALL
     SELECT 6 num UNION ALL
     SELECT 7 num UNION ALL
     SELECT 8 num UNION ALL
     SELECT 9 num) ones
     CROSS JOIN
     (SELECT 0 num UNION ALL
     SELECT 10 num UNION ALL
     SELECT 20 num UNION ALL
     SELECT 30 num UNION ALL
     SELECT 40 num UNION ALL
     SELECT 50 num UNION ALL
     SELECT 60 num UNION ALL
     SELECT 70 num UNION ALL
     SELECT 80 num UNION ALL
     SELECT 90 num) tens
     CROSS JOIN
     (SELECT 0 num UNION ALL
     SELECT 100 num UNION ALL
     SELECT 200 num UNION ALL
     SELECT 300 num) hundreds;
```
```
+------------------------------------+
| ones.num + tens.num + hundreds.num |
+------------------------------------+
|                                  0 |
|                                  1 |
|                                  2 |
|                                  3 |
|                                  4 |
|                                  5 |
|                                  6 |
|                                  7 |
|                                  8 |
|                                  9 |
|                                 10 |
|                                 11 |
|                                 12 |
...
...
...
|                                391 |
|                                392 |
|                                393 |
|                                394 |
|                                395 |
|                                396 |
|                                397 |
|                                398 |
|                                399 |
+------------------------------------+
400 rows in set (0.00 sec)
```
If you take the Cartesian product of the three sets {0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, {0, 10, 20, 30, 40, 50, 60, 70, 80, 90}, and {0, 100, 200, 300} and add the values in the three columns, you get a 400-row result set containing all numbers between 0 and 399. While this is more than the 366 rows needed to generate the set of days in 2020, it’s easy enough to get rid of the excess rows, and I’ll show you how shortly.

The next step is to convert the set of numbers to a set of dates. To do this, I will use the date_add() function to add each number in the result set to January 1, 2020. Then I’ll add a filter condition to throw away any dates that venture into 2021:

```sql
SELECT DATE_ADD('2020-01-01',
       INTERVAL (ones.num + tens.num + hundreds.num) DAY) dt
     FROM
      (SELECT 0 num UNION ALL
       SELECT 1 num UNION ALL
       SELECT 2 num UNION ALL
       SELECT 3 num UNION ALL
       SELECT 4 num UNION ALL
       SELECT 5 num UNION ALL
       SELECT 6 num UNION ALL
       SELECT 7 num UNION ALL
       SELECT 8 num UNION ALL
       SELECT 9 num) ones
       CROSS JOIN
      (SELECT 0 num UNION ALL
       SELECT 10 num UNION ALL
       SELECT 20 num UNION ALL
       SELECT 30 num UNION ALL
       SELECT 40 num UNION ALL
       SELECT 50 num UNION ALL
       SELECT 60 num UNION ALL
       SELECT 70 num UNION ALL
       SELECT 80 num UNION ALL
       SELECT 90 num) tens
       CROSS JOIN
      (SELECT 0 num UNION ALL
       SELECT 100 num UNION ALL
       SELECT 200 num UNION ALL
       SELECT 300 num) hundreds
     WHERE DATE_ADD('2020-01-01',
       INTERVAL (ones.num + tens.num + hundreds.num) DAY) < '2021-01-01'
     ORDER BY 1;
```
```
+------------+
| dt         |
+------------+
| 2020-01-01 |
| 2020-01-02 |
| 2020-01-03 |
| 2020-01-04 |
| 2020-01-05 |
| 2020-01-06 |
| 2020-01-07 |
| 2020-01-08 |
...
...
...
| 2020-02-26 |
| 2020-02-27 |
| 2020-02-28 |
| 2020-02-29 |
| 2020-03-01 |
| 2020-03-02 |
| 2020-03-03 |
...
...
...
| 2020-12-24 |
| 2020-12-25 |
| 2020-12-26 |
| 2020-12-27 |
| 2020-12-28 |
| 2020-12-29 |
| 2020-12-30 |
| 2020-12-31 |
+------------+
366 rows in set (0.03 sec)
```
이 접근 방식의 좋은 점은 데이터베이스 서버가 2020년 1월 1일에 59일을 더할 때 계산하기 때문에 결과 집합에 사용자의 개입 없이 자동으로 추가 윤일(2월 29일)이 포함된다는 것입니다.

이제 2020년 내내 날조할 수 있는 메커니즘이 생겼으니 어떻게 해야 할까요? 2020년에 그날의 영화 대여 횟수와 함께 매일 표시되는 보고서를 생성하라는 요청을 받을 수 있습니다. 보고서에는 영화를 대여하지 않은 날을 포함하여 연중 매일이 포함되어야 합니다. 쿼리는 다음과 같습니다(2005년을 사용하여 rent 테이블의 데이터와 일치).

```sql
SELECT days.dt, COUNT(r.rental_id) num_rentals
     FROM rental r
       RIGHT OUTER JOIN
      (SELECT DATE_ADD('2005-01-01',
         INTERVAL (ones.num + tens.num + hundreds.num) DAY) dt
       FROM
        (SELECT 0 num UNION ALL
         SELECT 1 num UNION ALL
         SELECT 2 num UNION ALL
         SELECT 3 num UNION ALL
         SELECT 4 num UNION ALL
         SELECT 5 num UNION ALL
         SELECT 6 num UNION ALL
         SELECT 7 num UNION ALL
         SELECT 8 num UNION ALL
         SELECT 9 num) ones
         CROSS JOIN
        (SELECT 0 num UNION ALL
         SELECT 10 num UNION ALL
         SELECT 20 num UNION ALL
         SELECT 30 num UNION ALL
         SELECT 40 num UNION ALL
         SELECT 50 num UNION ALL
         SELECT 60 num UNION ALL
         SELECT 70 num UNION ALL
         SELECT 80 num UNION ALL
         SELECT 90 num) tens
         CROSS JOIN
        (SELECT 0 num UNION ALL
         SELECT 100 num UNION ALL
         SELECT 200 num UNION ALL
         SELECT 300 num) hundreds
       WHERE DATE_ADD('2005-01-01',
         INTERVAL (ones.num + tens.num + hundreds.num) DAY) 
           < '2006-01-01'
      ) days
       ON days.dt = date(r.rental_date)
     GROUP BY days.dt
     ORDER BY 1;
```
```
+------------+-------------+
| dt         | num_rentals |
+------------+-------------+
| 2005-01-01 |           0 |
| 2005-01-02 |           0 |
| 2005-01-03 |           0 |
| 2005-01-04 |           0 |
...
| 2005-05-23 |           0 |
| 2005-05-24 |           8 |
| 2005-05-25 |         137 |
| 2005-05-26 |         174 |
| 2005-05-27 |         166 |
| 2005-05-28 |         196 |
| 2005-05-29 |         154 |
| 2005-05-30 |         158 |
| 2005-05-31 |         163 |
| 2005-06-01 |           0 |
...
| 2005-06-13 |           0 |
| 2005-06-14 |          16 |
| 2005-06-15 |         348 |
| 2005-06-16 |         324 |
| 2005-06-17 |         325 |
| 2005-06-18 |         344 |
| 2005-06-19 |         348 |
| 2005-06-20 |         331 |
| 2005-06-21 |         275 |
| 2005-06-22 |           0 |
...
| 2005-12-27 |           0 |
| 2005-12-28 |           0 |
| 2005-12-29 |           0 |
| 2005-12-30 |           0 |
| 2005-12-31 |           0 |
+------------+-------------+
365 rows in set (8.99 sec)
```
이것은 교차 조인, 외부 조인, 날짜 함수, 그룹화, 집합 연산(모두 통합) 및 집계 함수(count())를 포함한다는 점에서 이 책에서 지금까지 가장 흥미로운 쿼리 중 하나입니다. 또한 주어진 문제에 대한 가장 우아한 해결책은 아니지만 약간의 창의성과 언어에 대한 확고한 이해로 어떻게 크로스 조인과 같은 거의 사용하지 않는 기능도 강력한 도구로 만들 수 있는지에 대한 예가 되어야 합니다. 당신의 SQL 툴킷에서.

### natural 조인
당신이 게으른 경우(그리고 우리 모두가 아닌 경우), 조인할 테이블의 이름을 지정할 수 있지만 데이터베이스 서버가 필요한 조인 조건을 결정할 수 있도록 하는 조인 유형을 선택할 수 있습니다. 자연 조인으로 알려진 이 조인 유형은 여러 테이블에서 동일한 열 이름을 사용하여 적절한 조인 조건을 유추합니다. 예를 들어, rent 테이블에는 customer_id라는 열이 포함되어 있습니다. 이 열은 customer_id라는 이름의 기본 키도 포함된 customer 테이블에 대한 외래 키입니다. 따라서 natural 조인을 사용하여 두 테이블을 조인하는 쿼리를 작성할 수 있습니다.

```sql
SELECT c.first_name, c.last_name, date(r.rental_date)
     FROM customer c
       NATURAL JOIN rental r;
```
```
Empty set (0.04 sec)
```
natural 조인을 지정했기 때문에 서버는 테이블 정의를 검사하고 조인 조건 r.customer_id = c.customer_id를 추가하여 두 테이블을 조인했습니다. 이것은 잘 작동했지만 Sakila 스키마에서 모든 테이블에는 각 행이 마지막으로 수정된 시간을 표시하는 last_update 열이 포함되어 있으므로 서버는 조인 조건 r.last_update = c.last_update도 추가하여 쿼리가 다음을 수행하도록 합니다. 데이터를 반환하지 않습니다.

이 문제를 해결하는 유일한 방법은 서브쿼리를 사용하여 테이블 중 하나 이상에 대한 열을 제한하는 것입니다.

```sql
SELECT cust.first_name, cust.last_name, date(r.rental_date)
     FROM
      (SELECT customer_id, first_name, last_name
       FROM customer
      ) cust
       NATURAL JOIN rental r;
```
```
+------------+-----------+---------------------+
| first_name | last_name | date(r.rental_date) |
+------------+-----------+---------------------+
| MARY       | SMITH     | 2005-05-25          |
| MARY       | SMITH     | 2005-05-28          |
| MARY       | SMITH     | 2005-06-15          |
| MARY       | SMITH     | 2005-06-15          |
| MARY       | SMITH     | 2005-06-15          |
| MARY       | SMITH     | 2005-06-16          |
| MARY       | SMITH     | 2005-06-18          |
| MARY       | SMITH     | 2005-06-18          |
...
| AUSTIN     | CINTRON   | 2005-08-21          |
| AUSTIN     | CINTRON   | 2005-08-21          |
| AUSTIN     | CINTRON   | 2005-08-21          |
| AUSTIN     | CINTRON   | 2005-08-23          |
| AUSTIN     | CINTRON   | 2005-08-23          |
| AUSTIN     | CINTRON   | 2005-08-23          |
+------------+-----------+---------------------+
16044 rows in set (0.03 sec)
```
So, is the reduced wear and tear on the old fingers from not having to type the join condition worth the trouble? Absolutely not; you should avoid this join type and use inner joins with explicit join conditions.

## Test Your Knowledge

The following exercises test your understanding of outer and cross joins. Please see Appendix B for solutions.

Exercise 10-1
Using the following table definitions and data, write a query that returns each customer name along with their total payments:
```
			Customer:
Customer_id  	Name
-----------  	---------------
1		John Smith
2		Kathy Jones
3		Greg Oliver

			Payment:
Payment_id	Customer_id	Amount
----------	-----------	--------
101		1		8.99
102		3		4.99
103		1		7.99
```
Include all customers, even if no payment records exist for that customer.

**Exercise 10-2**
Reformulate your query from Exercise 10-1 to use the other outer join type (e.g., if you used a left outer join in Exercise 10-1, use a right outer join this time) such that the results are identical to Exercise 10-1.

**Exercise 10-3 (Extra Credit)**
Devise a query that will generate the set {1, 2, 3, ..., 99, 100}. (Hint: use a cross join with at least two from clause subqueries.)
