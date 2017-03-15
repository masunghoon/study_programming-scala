# 20 스칼라 도메인 특화 언어

DSL: 대상 분야의 전문가들이 사용하는 용어, 상용구, 표현을 닮은 프로그래밍 언어
- 구조화된 문서처럼 읽을 수 있음

#### 장점
- **캡슐화**: 자세한 구현을 감추고, 해당 분야와 관련있는 추상적인 부분만 노출
- **높은 생산성**: *캡슐화* 덕분에 어플리케이션의 작성과 변경에 드는 노력 최소화
- **커뮤니케이션**: *개발자* 는 해당 분야를 이해하고, *전문가* 는 구현이 요구사항을 만족하는지 쉽게 확인 가능

#### 단점
- **구현이 어려움**: 전통적인 API보다 설계가 어렵다
- **유지보수 어려움**: 구현이 단순하지 않기 떄문에 유지보수도 덩달아 힘들다

#### 구현 관점에서 구분
- **내부 DSL**: 범용 프로그래밍 언어에서 코드를 관용구처럼 사용해 작성
  - 따로 구문분석기가 필요하지 않기에 작성이 더 쉬움
  - 기저 언어의 제약사항이 대상 분야의 개념 표현을 위한 선택 사항 제약 가능
- **외부 DSL**: 자체 문법과 구문분석기를 사용하는 별도의 전용 언어
  - *신뢰 가능한* 구문분석기를 만들 수 있는 한 원하는 대로 언어 설계 가능
  - 사용자에게 도움이 되는 오류메시지 표현 등의 어려움

## 20.1 예제: 스칼라를 위한 XML과 JSON DSL

[reading.sc](../src/main/scala/dsls/xml/reading.sc)

[writing.sc](../src/main/scala/dsls/xml/writing.sc)

#### Scala의 `json`
- 스칼라 **파서 콤비네이터** 라이브러리
  - `json` 구문분석에 대한 제한적인 지원 추가 (20.3)

- 자바 외에서 괜찮은 `json`라이브러리 다수 존재
- 스칼라가 기본 제공하는 `json`지원은 제한적인 필요상황에만 고려
- 사용 중인 프레임워크가 있다면, 그 프레임워크와 궁합이 잘 맞는 라이브러리가 있으니 선택에 참조
- or ... **Googling**!

## 20.2 내부 DSL
> 스칼라 문법의 여러 특징이 **내부 DSL** 의 구현에 도움

##### 유연한 명명 규칙
- 네이밍에 특별한 제약사항이 없음 (특수문자 허용)
- 대수적인 특성이 있는 타입에 대수 기호를 사용하는 등 해당 분야에 알맞는 이름 사용
  - ex> `Matrix`타입에 행렬곱 메서드는 `*` 로 명명 가능

##### 중위&후위 표기법
- 중위 표기가 가능하기에 `*`메서드가 `matrix1 * matrix2`처럼 의미가 있다
- `1 minute` 등 후위 표기법도 유용

##### 암시적 인자와 기본 인자값
- boilerplate를 줄이고, 복잡한 세부 사항 숨김
  - DSL의 모든 메서드에 전달해야 하는 어떤 맥락을 담은 인자가 있는 경우 이를 `implicit`으로 처리
- ex> `Future`의 여러 메서드

##### 타입 클래스
- 기존 타입에 메서드를 *추가* 하기 위한 암시적 변환 사용가능
- ex> [`scala.concurrent.duration`](http://bit.ly/10884Ty) 패키지 사용시 75초에 해당하는 [`FiniteDuration`](http://bit.ly/1nWnnJN) 표현시 `1.25 minutes`와 같이 표현 가능

##### 동적 메서드 호출
- [`Dynamic`](http://bit.ly/1DDdSBj) 트레이트로 객체에 임의의 메서드나 필드 호출 가능
- 타입에 해당 이름으로 선언한 메서드나 필드가 없는 경우에도 처리 가능

##### 고차 함수와 이름에 의한 호출
- 언어 고유의 제어 구조처럼 보이는 DSL을 만들 수 있게 해줌
- 3.10절의 `continue` 예제

##### 자기 타입 표기
- DSL구현에 내포된 부분에서 자신을 둘러싼 인스턴스 참조 가능
- 자신을 둘러싼 영역의 상태 객체를 변경하는데 활용 가능

##### 매크로
- 일부 고급 시나리오는 새로운 **매크로** 기능을 사용해 구현 가능 (24장 참조)

#### 예제
- 급여 계산 애플리케이션에서 매 주기(2주)마다 직원 급여를 계산하기 위해 사용할 내부 DSL
- **총 급여** 에서 **공제엑**(세금, 각종 보험금, 퇴직연금 등)을 차감한 **순 급여** 계산

[common.scala](../src/main/scala/dsls/payroll/common.scala)

[dsl.scala](../src/main/scala/dsls/payroll/internal/dsl.scala)

#### 문제점

##### 스칼라의 문법을 교묘히 활용하는 데 너무 의존한다

##### 문법의 규칙이 임의적

##### 오류 메시지가 형편없음

##### DSL이 사용자의 오용을 막지 못함

##### 변경 가능한 인스턴스를 사용


## 20.3 파서 콤비네이터를 활용한 외부 DSL

### 20.3.1 파서 콤비네이터
- 컬렉션 콤비네이터처럼 구문분석기를 만들기 위한 구성 요소
- 입력에서 부동소수점수나 정수 등 정해진 부분을 처리하는 구문분석기를 조합하여 더 큰 식을 처리하는 구문분석기 생성
- 좋은 구문분석기 라이브러리는 *순차 구분* 이나 *대안*, *반복*, *선택적인 구문* 등을 지원

### 20.3.2 급예 계산 외부 DSL

[dsl.scala](../src/main/scala/dsls/payroll/parsercomb/dsl.scala)


## 20.4 내부 DSL과 외부 DSL에 대한 마지막 고찰
```scala
val biweeklyDeductions = biweekly { deduct =>
  deduct federal_tax        (25.0   percent)
  deduct state_tax          (5.0    percent)
  deduct insurance_premiums (500.0  dollars)
  deduct retirement_savings (10.0   percent)
}
```

```scala
val input = """biweekly {
  federal tax         20.0  percent,
  state tax           3.0   percent,
  insurance premiums  250.0 dollars,
  retirement savings  15.0. percent
}"""
```

## 20.5 마치며
- DSL은 흥미롭지만, 노력과 비용이 많이 들 수 있다

#### 배울 것
- 스칼라 도구와 라이브러리