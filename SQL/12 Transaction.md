
# 12장. 트랜잭션

지금까지 이 책의 모든 예제는 개별적이고 독립적인 SQL 문이었습니다. 이것은 임시 보고 또는 데이터 유지 관리 스크립트의 표준일 수 있지만 응용 프로그램 논리에는 논리적 작업 단위로 함께 실행해야 하는 여러 SQL 문이 포함되는 경우가 많습니다. 이 장에서는 SQL 문 집합을 그룹화하여 모든 문이 성공하거나 전혀 성공하지 않도록 하는 메커니즘인 트랜잭션을 살펴봅니다.

## 다중 사용자 데이터베이스

데이터베이스 관리 시스템을 사용하면 단일 사용자가 데이터를 쿼리하고 수정할 수 있지만 오늘날에는 수천 명의 사람들이 동시에 데이터베이스를 변경할 수 있습니다. 모든 사용자가 일반 업무 시간 동안 데이터 웨어하우스의 경우와 같이 쿼리만 실행하는 경우 데이터베이스 서버가 처리할 문제는 거의 없습니다. 그러나 일부 사용자가 데이터를 추가 및/또는 수정하는 경우 서버는 훨씬 더 많은 부기를 처리해야 합니다.

예를 들어, 현재 주의 영화 대여 활동을 요약한 보고서를 실행하고 있다고 가정해 보겠습니다. 그러나 보고서를 실행하는 동시에 다음 활동이 발생하고 있습니다.

- 고객이 필름을 대여합니다.
- 고객이 기한이 지난 후 필름을 반납하고 연체료를 지불한 경우.
- 인벤토리에 신규 영화 5종이 추가됩니다.

따라서 보고서가 실행되는 동안 여러 사용자가 기본 데이터를 수정하고 있으므로 보고서에 표시되어야 하는 숫자는 무엇입니까? 대답은 다음 섹션에서 설명하는 잠금을 서버가 처리하는 방법에 따라 다릅니다.

## 잠금

잠금은 데이터베이스 서버가 데이터 자원의 동시 사용을 제어하는 ​​데 사용하는 메커니즘입니다. 데이터베이스의 일부가 잠겨 있을 때 해당 데이터를 수정(또는 읽기)하려는 다른 사용자는 잠금이 해제될 때까지 기다려야 합니다. 대부분의 데이터베이스 서버는 다음 두 가지 잠금 전략 중 하나를 사용합니다.

데이터베이스 작성자는 데이터를 수정하기 위해 서버에서 쓰기 잠금을 요청하고 수신해야 하며, 데이터베이스 리더는 데이터를 쿼리하기 위해 서버에서 읽기 잠금을 요청하고 수신해야 합니다. 여러 사용자가 동시에 데이터를 읽을 수 있지만 각 테이블(또는 그 일부)에 대해 한 번에 하나의 쓰기 잠금만 제공되고 쓰기 잠금이 해제될 때까지 읽기 요청이 차단됩니다.

데이터베이스 작성자는 데이터를 수정하기 위해 서버에서 쓰기 잠금을 요청하고 수신해야 하지만 판독기는 데이터를 쿼리하기 위해 어떤 유형의 잠금도 필요하지 않습니다. 대신 서버는 독자가 쿼리가 시작될 때부터 쿼리가 완료될 때까지 독자가 데이터의 일관된 보기(다른 사용자가 수정하더라도 데이터는 동일하게 보임)를 볼 수 있도록 합니다. 이 접근 방식을 버전 관리라고 합니다.

두 접근 방식 모두 장단점이 있습니다. 첫 번째 접근 방식은 동시 읽기 및 쓰기 요청이 많은 경우 대기 시간이 길어질 수 있고, 두 번째 접근 방식은 데이터가 수정되는 동안 오래 실행되는 쿼리가 있는 경우 문제가 될 수 있습니다. 이 책에서 논의한 세 대의 서버 중 Microsoft SQL Server는 첫 번째 접근 방식을 사용하고 Oracle Database는 두 번째 접근 방식을 사용하며 MySQL은 두 가지 접근 방식을 모두 사용합니다(선택한 스토리지 엔진에 따라 다름, 이 장의 뒷부분에서 설명함). .
### Lock Granularities
리소스를 잠그는 방법을 결정할 때 사용할 수 있는 다양한 전략도 있습니다. 서버는 세 가지 다른 수준 또는 세분성 중 하나에서 잠금을 적용할 수 있습니다.

- **테이블 잠금**
여러 사용자가 동일한 테이블의 데이터를 동시에 수정하지 못하도록 방지

- **페이지 잠금**
여러 사용자가 테이블의 동일한 페이지(페이지는 일반적으로 2KB에서 16KB 범위의 메모리 세그먼트임)의 데이터를 동시에 수정하지 못하도록 합니다.

- **행 잠금**
여러 사용자가 테이블의 동일한 행을 동시에 수정하지 못하도록 방지

다시 말하지만, 이러한 접근 방식에는 장단점이 있습니다. 전체 테이블을 잠그는 데는 부기가 거의 필요하지 않지만 이 접근 방식은 사용자 수가 증가함에 따라 허용할 수 없는 대기 시간을 빠르게 생성합니다. 반면에 행 잠금은 훨씬 더 많은 부기가 필요하지만 많은 사용자가 다른 행에 관심이 있는 한 동일한 테이블을 수정할 수 있습니다. 이 책에서 논의된 3개의 서버 중 Microsoft SQL Server는 페이지, 행 및 테이블 잠금을 사용하고 Oracle Database는 행 잠금만 사용하며 MySQL은 테이블, 페이지 또는 행 잠금을 사용합니다(역시 스토리지 엔진 선택에 따라 다름). . SQL Server는 특정 상황에서 행에서 페이지로, 페이지에서 테이블로 잠금을 에스컬레이션하지만 Oracle Database는 잠금을 에스컬레이션하지 않습니다.

보고서로 돌아가기 위해 보고서 페이지에 표시되는 데이터는 보고서가 시작될 때의 데이터베이스 상태(서버에서 버전 관리 방식을 사용하는 경우) 또는 서버에서 보고를 실행할 때의 데이터베이스 상태를 미러링합니다. 응용 프로그램 읽기 잠금(서버에서 읽기 및 쓰기 잠금을 모두 사용하는 경우).

## 트랜잭션이란 무엇입니까?

데이터베이스 서버가 100% 가동 시간을 즐기고, 사용자가 항상 프로그램 실행을 완료하도록 허용하고, 응용 프로그램이 실행을 중지하는 치명적인 오류가 발생하지 않고 항상 완료된다면, 동시 데이터베이스 액세스에 대해 논의할 것이 없을 것입니다. 그러나 우리는 이들 중 어느 것에도 의존할 수 없으므로 여러 사용자가 동일한 데이터에 액세스할 수 있도록 하려면 하나의 요소가 더 필요합니다.

동시성 퍼즐의 이 추가 조각은 여러 SQL 문을 그룹화하여 모든 문이 성공하거나 전혀 성공하지 않도록 하는 장치인 트랜잭션입니다(원자성으로 알려진 속성). 저축 계좌에서 당좌 예금 계좌로 $500를 이체하려고 시도하는 경우, 저축 계좌에서 성공적으로 인출되었지만 당좌 계좌로 입금되지 않았다면 약간 당황할 것입니다. 실패 이유가 무엇이든(유지 관리를 위해 서버가 종료되었거나 계정 테이블에 대한 페이지 잠금 요청이 시간 초과됨 등) 500달러를 돌려받기를 원합니다.

이러한 종류의 오류를 방지하기 위해 송금 요청을 처리하는 프로그램은 먼저 거래를 시작한 다음 저축에서 당좌예금으로 돈을 이동하는 데 필요한 SQL 문을 발행하고 모든 것이 성공하면 다음을 발행하여 거래를 종료합니다. 커밋 명령. 그러나 예상치 못한 일이 발생하면 프로그램은 트랜잭션이 시작된 이후에 이루어진 모든 변경 사항을 취소하도록 서버에 지시하는 롤백 명령을 발행합니다. 전체 프로세스는 다음과 같을 수 있습니다.
```sql
START TRANSACTION;

 /* withdraw money from first account, making sure balance is sufficient */
UPDATE account SET avail_balance = avail_balance - 500
WHERE account_id = 9988
  AND avail_balance > 500;

IF <exactly one row was updated by the previous statement> THEN
  /* deposit money into second account */
  UPDATE account SET avail_balance = avail_balance + 500
    WHERE account_id = 9989;

  IF <exactly one row was updated by the previous statement> THEN
    /* everything worked, make the changes permanent */
    COMMIT;
  ELSE
    /* something went wrong, undo all changes in this transaction */
    ROLLBACK;
  END IF;
ELSE
  /* insufficient funds, or error encountered during update */
  ROLLBACK;
END IF;
```
---
NOTE
이전 코드 블록은 Oracle의 PL/SQL 또는 Microsoft의 Transact-SQL과 같은 주요 데이터베이스 회사에서 제공하는 절차적 언어 중 하나와 유사하게 보일 수 있지만 의사 코드로 작성되었으며 특정 언어를 모방하려고 시도하지 않습니다.

---
이전 코드 블록은 거래를 시작하는 것으로 시작한 다음 당좌 예금 계좌에서 $500를 빼서 저축 계좌에 추가하려고 시도합니다. 모든 것이 순조롭게 진행되면 트랜잭션이 커밋됩니다. 그러나 문제가 발생하면 트랜잭션이 롤백됩니다. 즉, 트랜잭션 시작 이후의 모든 데이터 변경 사항이 취소됩니다.

거래를 사용함으로써 프로그램은 $500가 균열에 빠질 가능성 없이 저축 계좌에 남아 있거나 당좌 예금 계좌로 이체되도록 합니다. 트랜잭션이 커밋되었거나 롤백되었는지 여부에 관계없이 트랜잭션 실행 중에 획득한 모든 리소스(예: 쓰기 잠금)는 트랜잭션이 완료되면 해제됩니다.

물론 프로그램이 두 업데이트 문을 모두 완료할 수 있지만 커밋이나 롤백이 실행되기 전에 서버가 종료되면 서버가 다시 온라인 상태가 될 때 트랜잭션이 롤백됩니다. (데이터베이스 서버가 온라인 상태가 되기 전에 완료해야 하는 작업 중 하나는 서버가 종료될 때 진행 중인 불완전한 트랜잭션을 찾아 롤백하는 것입니다.) 또한 프로그램이 트랜잭션을 완료하고 커밋을 실행하지만 서버가 종료되는 경우 변경 사항이 영구 저장소에 적용되기 전에(즉, 수정된 데이터가 메모리에 있지만 디스크로 플러시되지 않은 경우) 데이터베이스 서버는 서버가 다시 시작될 때 트랜잭션의 변경 사항을 다시 적용해야 합니다(속성으로 알려진 속성 내구성).

### 트랜잭션 시작

데이터베이스 서버는 다음 두 가지 방법 중 하나로 트랜잭션 생성을 처리합니다.

- 활성 트랜잭션은 항상 데이터베이스 세션과 연결되므로 트랜잭션을 명시적으로 시작할 필요나 방법이 없습니다. 현재 트랜잭션이 종료되면 서버는 자동으로 세션에 대한 새 트랜잭션을 시작합니다.

- 트랜잭션을 명시적으로 시작하지 않는 한 개별 SQL 문은 서로 독립적으로 자동 커밋됩니다. 트랜잭션을 시작하려면 먼저 명령을 실행해야 합니다.

3개의 서버 중 Oracle Database는 첫 번째 접근 방식을 취하고 Microsoft SQL Server와 MySQL은 두 번째 접근 방식을 취합니다. Oracle의 트랜잭션 접근 방식의 장점 중 하나는 단일 SQL 명령만 실행하더라도 결과가 마음에 들지 않거나 마음이 바뀌면 변경 사항을 롤백할 수 있다는 것입니다. 따라서 delete 문에 where 절을 추가하는 것을 잊은 경우 손상을 취소할 수 있는 기회가 있습니다(모닝 커피를 마셨고 테이블의 125,000개 행을 모두 삭제할 의도가 아니었다는 것을 깨달았다고 가정) . 그러나 MySQL과 SQL Server를 사용하면 Enter 키를 누르면 SQL 문으로 인한 변경 사항이 영구적입니다(DBA가 백업이나 다른 방법에서 원본 데이터를 검색할 수 없는 경우).

SQL:2003 표준에는 트랜잭션을 명시적으로 시작하려는 경우 사용할 트랜잭션 시작 명령이 포함되어 있습니다. MySQL이 표준을 준수하는 동안 SQL Server 사용자는 대신 begin transaction 명령을 실행해야 합니다. 두 서버 모두에서 트랜잭션을 명시적으로 시작할 때까지 자동 커밋 모드라고 하는 모드에 있게 됩니다. 이는 개별 명령문이 서버에서 자동으로 커밋됨을 의미합니다. 따라서 트랜잭션에 참여하기로 결정하고 트랜잭션 시작/시작 명령을 실행하거나 단순히 서버가 개별 명령문을 커밋하도록 할 수 있습니다.

MySQL과 SQL Server는 모두 개별 세션에 대해 자동 커밋 모드를 끌 수 있도록 하며, 이 경우 서버는 트랜잭션과 관련하여 Oracle Database처럼 작동합니다. SQL Server에서 다음 명령을 실행하여 자동 커밋 모드를 비활성화합니다.
```
IMPLICIT_TRANSACTIONS 설정
```
MySQL을 사용하면 다음을 통해 자동 커밋 모드를 비활성화할 수 있습니다.
```
SET AUTOCOMMIT=0
```
자동 커밋 모드를 종료하면 모든 SQL 명령은 트랜잭션 범위 내에서 발생하며 명시적으로 커밋하거나 롤백해야 합니다.

---
노트

조언: 로그인할 때마다 자동 커밋 모드를 끄고 트랜잭션 내에서 모든 SQL 문을 실행하는 습관을 들이십시오. 다른 방법이 없다면 실수로 삭제한 데이터를 재구성하도록 DBA에게 요청해야 하는 당혹감을 덜어줄 수 있습니다.

---

### 트랜잭션 종료

트랜잭션 시작 명령을 통해 명시적으로 또는 데이터베이스 서버에 의해 암시적으로 트랜잭션이 시작되면 변경 사항이 영구적으로 적용되도록 트랜잭션을 명시적으로 종료해야 합니다. 서버에 변경 사항을 영구적으로 표시하고 트랜잭션 중에 사용된 리소스(즉, 페이지 또는 행 잠금)를 해제하도록 지시하는 commit 명령을 통해 이 작업을 수행합니다.

트랜잭션을 시작한 이후의 모든 변경 사항을 취소하려면 롤백 명령을 실행해야 합니다. 이 명령은 서버가 데이터를 트랜잭션 전 상태로 되돌리도록 지시합니다. 롤백이 완료되면 세션에서 사용하는 모든 리소스가 해제됩니다.

커밋 또는 롤백 명령을 실행하는 것과 함께 작업의 간접적인 결과 또는 제어할 수 없는 결과로 트랜잭션이 종료될 수 있는 몇 가지 다른 시나리오가 있습니다.

- 서버가 종료되면 서버가 다시 시작될 때 트랜잭션이 자동으로 롤백됩니다.

- 현재 트랜잭션이 커밋되고 새 트랜잭션이 시작되도록 하는 alter table과 같은 SQL 스키마 문을 실행합니다.

- 다른 트랜잭션 시작 명령을 실행하면 이전 트랜잭션이 커밋됩니다.

서버가 교착 상태를 감지하고 트랜잭션이 범인이라고 결정하기 때문에 서버가 트랜잭션을 조기에 종료합니다. 이 경우 트랜잭션이 롤백되고 오류 메시지가 표시됩니다.

이 네 가지 시나리오 중 첫 번째와 세 번째 시나리오는 매우 간단하지만 다른 두 시나리오는 논의할 가치가 있습니다. 두 번째 시나리오에 관한 한 새 테이블이나 인덱스를 추가하거나 테이블에서 열을 제거하는 데이터베이스 변경은 롤백할 수 없으므로 스키마를 변경하는 명령은 외부에서 이루어져야 합니다. 거래. 따라서 현재 트랜잭션이 진행 중인 경우 서버는 현재 트랜잭션을 커밋하고 SQL 스키마 명령문 명령을 실행한 다음 세션에 대한 새 트랜잭션을 자동으로 시작합니다. 서버는 무슨 일이 일어났는지 알려주지 않으므로 작업 단위를 구성하는 명령문이 서버에 의해 실수로 여러 트랜잭션으로 분할되지 않도록 주의해야 합니다.

네 번째 시나리오는 교착 상태 감지를 다룹니다. 교착 상태는 두 개의 다른 트랜잭션이 다른 트랜잭션이 현재 보유하고 있는 리소스를 기다리고 있을 때 발생합니다. 예를 들어, 트랜잭션 A는 방금 계정 테이블을 업데이트하고 트랜잭션 테이블에 대한 쓰기 잠금을 기다리는 반면 트랜잭션 B는 트랜잭션 테이블에 행을 삽입하고 계정 테이블에 대한 쓰기 잠금을 기다리고 있습니다. 두 트랜잭션이 동일한 페이지 또는 행을 수정하는 경우(데이터베이스 서버에서 사용하는 잠금 단위에 따라 다름), 각각은 다른 트랜잭션이 완료되고 필요한 리소스를 해제할 때까지 영원히 기다립니다. 데이터베이스 서버는 처리량이 중단되지 않도록 항상 이러한 상황을 경계해야 합니다. 교착 상태가 감지되면 트랜잭션 중 하나가 선택되어(임의로 또는 일부 기준에 따라) 다른 트랜잭션이 진행될 수 있도록 롤백됩니다. 대부분의 경우 종료된 트랜잭션은 다시 시작할 수 있으며 다른 교착 상태가 발생하지 않고 성공합니다.

앞서 논의한 두 번째 시나리오와 달리 데이터베이스 서버는 교착 상태 감지로 인해 트랜잭션이 롤백되었음을 알리는 오류를 발생시킵니다. 예를 들어 MySQL의 경우 다음 메시지를 포함하는 오류 1213이 수신됩니다.

메시지: 잠금을 시도하는 동안 교착 상태가 발견되었습니다. 트랜잭션을 다시 시작하십시오
오류 메시지에서 알 수 있듯이 교착 상태 감지로 인해 롤백된 트랜잭션을 재시도하는 것이 합리적입니다. 그러나 교착 상태가 상당히 일반적인 경우 교착 상태의 가능성을 줄이기 위해 데이터베이스에 액세스하는 응용 프로그램을 수정해야 할 수도 있습니다(한 가지 일반적인 전략은 계정 데이터를 항상 수정하는 것과 같이 데이터 리소스가 항상 동일한 순서로 액세스되도록 하는 것입니다 트랜잭션 데이터를 삽입하기 전에).

### 트랜잭션 저장점

경우에 따라 롤백이 필요한 트랜잭션 내에서 문제가 발생할 수 있지만 발생한 모든 작업을 실행 취소하고 싶지 않을 수 있습니다. 이러한 상황의 경우 트랜잭션 내에서 하나 이상의 저장점을 설정하고 이를 사용하여 트랜잭션 시작 부분으로 완전히 롤백하지 않고 트랜잭션 내의 특정 위치로 롤백할 수 있습니다.

### CHOOSING A STORAGE ENGINE

When using Oracle Database or Microsoft SQL Server, a single set of code is responsible for low-level database operations, such as retrieving a particular row from a table based on primary key value. The MySQL server, however, has been designed so that multiple storage engines may be utilized to provide low-level database functionality, including resource locking and transaction management. As of version 8.0, MySQL includes the following storage engines:

**MyISAM**
A nontransactional engine employing table locking

**MEMORY**
A nontransactional engine used for in-memory tables

**CSV**
A transactional engine that stores data in comma-separated files

**InnoDB**
A transactional engine employing row-level locking

**Merge**
A specialty engine used to make multiple identical MyISAM tables appear as a single table (a.k.a. table partitioning)

**Archive**
A specialty engine used to store large amounts of unindexed data, mainly for archival purposes

데이터베이스에 대해 단일 스토리지 엔진을 선택해야 한다고 생각할 수도 있지만 MySQL은 테이블별로 스토리지 엔진을 선택할 수 있을 만큼 충분히 유연합니다. 그러나 트랜잭션에 참여할 수 있는 모든 테이블의 경우 행 수준 잠금 및 버전 관리를 사용하여 다양한 스토리지 엔진에서 최고 수준의 동시성을 제공하는 InnoDB 엔진을 선택해야 합니다.

테이블을 생성할 때 스토리지 엔진을 명시적으로 지정하거나 기존 테이블을 변경하여 다른 엔진을 사용할 수 있습니다. 테이블에 할당된 엔진을 모르는 경우 다음과 같이 show table 명령을 사용할 수 있습니다.

```sql
mysql> show table status like 'customer' \G;
```
```
*************************** 1. row ***************************
           Name: customer
         Engine: InnoDB
        Version: 10
     Row_format: Dynamic
           Rows: 599
 Avg_row_length: 136
    Data_length: 81920
Max_data_length: 0
   Index_length: 49152
      Data_free: 0
 Auto_increment: 599
    Create_time: 2019-03-12 14:24:46
    Update_time: NULL
     Check_time: NULL
      Collation: utf8_general_ci
       Checksum: NULL
 Create_options:
        Comment:
1 row in set (0.16 sec)
```
Looking at the second item, you can see that the customer table is already using the InnoDB engine. If it were not, you could assign the InnoDB engine to the transaction table via the following command:
```sql
ALTER TABLE customer ENGINE = INNODB;
```

All savepoints must be given a name, which allows you to have multiple savepoints within a single transaction. To create a savepoint named my_savepoint, you can do the following:

```sql
SAVEPOINT my_savepoint;
```
To roll back to a particular savepoint, you simply issue the rollback command followed by the keywords to savepoint and the name of the savepoint, as in:
```
ROLLBACK TO SAVEPOINT my_savepoint;
```
Here’s an example of how savepoints may be used:
```sql
START TRANSACTION;

UPDATE product
SET date_retired = CURRENT_TIMESTAMP()
WHERE product_cd = 'XYZ';

SAVEPOINT before_close_accounts;

UPDATE account
SET status = 'CLOSED', close_date = CURRENT_TIMESTAMP(),
  last_activity_date = CURRENT_TIMESTAMP()
WHERE product_cd = 'XYZ';

ROLLBACK TO SAVEPOINT before_close_accounts;
COMMIT;
```
The net effect of this transaction is that the mythical XYZ product is retired but none of the accounts are closed.

When using savepoints, remember the following:

- Despite the name, nothing is saved when you create a savepoint. You must eventually issue a commit if you want your transaction to be made permanent.

- If you issue a rollback without naming a savepoint, all savepoints within the transaction will be ignored, and the entire transaction will be undone.

- If you are using SQL Server, you will need to use the proprietary command save transaction to create a savepoint and rollback transaction to roll back to a savepoint, with each command being followed by the savepoint name.

## Test Your Knowledge

Test your understanding of transactions by working through the following exercise. When you’re done, compare your solution with that in Appendix B.

Exercise 12-1

Generate a unit of work to transfer $50 from account 123 to account 789. You will need to insert two rows into the transaction table and update two rows in the account table. Use the following table definitions/data:
```
			Account:
account_id	avail_balance	last_activity_date
----------	-------------	------------------
123		500		2019-07-10 20:53:27
789		75		2019-06-22 15:18:35

			Transaction:
txn_id		txn_date	account_id	txn_type_cd	amount
---------	------------	-----------	-----------	--------
1001		2019-05-15	123		C		500
1002		2019-06-01	789		C		75
```
Use txn_type_cd = 'C' to indicate a credit (addition), and use txn_type_cd = 'D' to indicate a debit (subtraction).