# Chain of Custody

Кайралиев

Программа представляет собой консольное приложение для работы с объектами:
- `Sample`
- `Seal`
- `CustodyEvent`

## Структура проекта

- `model` — классы предметной области
- `validation` — проверка входных данных
- `service` — менеджеры объектов и бизнес-логика
- `cli` — командный интерпретатор и команды


## Общие правила ввода

- Программа работает в цикле и ожидает ввод команд до `exit`
- Угловые скобки в командах вводить не нужно
- Идентификаторы (`sample_id`, `seal_id`, `event_id`) должны быть целыми положительными числами
- Опция `--last N` требует положительное целое число `N`

- Для полей `from` и `to` допускаются только буквы, пробел и дефис
- Пустые обязательные поля не допускаются

## Команды

### Общие команды

- `help` — вывести список доступных команд
- `exit` — завершить работу программы

### Команды для `Sample`

- `sample_add`
  Создать новый `Sample`
  Ожидаемый ввод: интерактивно запрашивается `name` (`string`)

- `sample_list`
  Вывести список всех `Sample`
  Ожидаемый ввод: аргументы не требуются

- `sample_show <sample_id>`
  Вывести один `Sample` по идентификатору
  Ожидаемый ввод: `sample_id` (`long`)

- `sample_update <sample_id>`
  Обновить имя `Sample`
  Ожидаемый ввод: `sample_id` (`long`), затем новое `name` (`string`)

- `sample_remove <sample_id>`
  Удалить `Sample`
  Ожидаемый ввод: `sample_id` (`long`)

- `sample_hold <sample_id>`
  Перевести `Sample` в состояние `ON_HOLD`
  Ожидаемый ввод: `sample_id` (`long`)

- `sample_release <sample_id>`
  Перевести `Sample` в состояние `ACTIVE`
  Ожидаемый ввод: `sample_id` (`long`)

### Команды для `Seal`

- `seal_add <sample_id>`
  Создать новую `Seal` для указанного `Sample`
  Ожидаемый ввод: `sample_id` (`long`), затем `sealNumber` (`string`)

- `seal_list`
  Вывести список всех `Seal`
  Ожидаемый ввод: аргументы не требуются

- `seal_show <seal_id>`
  Вывести одну `Seal` по идентификатору
  Ожидаемый ввод: `seal_id` (`long`)

- `seal_update <seal_id>`
  Обновить номер пломбы
  Ожидаемый ввод: `seal_id` (`long`), затем новый `sealNumber` (`string`)

- `seal_remove <seal_id>`
  Удалить `Seal`
  Ожидаемый ввод: `seal_id` (`long`)

- `seal_break <seal_id>`
  Перевести `Seal` в состояние `BROKEN`
  Ожидаемый ввод: `seal_id` (`long`)

### Команды для `CustodyEvent`

- `cust_add <sample_id>`
  Создать новое событие передачи `CustodyEvent`
  Ожидаемый ввод:
  `sample_id` (`long`), затем интерактивно:
  `from` (`string`), `to` (`string`), `location` (`string`), `comment` (`string`, необязательное поле)

- `cust_list <sample_id>`
  Вывести все события передачи для указанного `Sample`
  Ожидаемый ввод: `sample_id` (`long`)

- `cust_list <sample_id> --last N`
  Вывести последние `N` событий передачи для указанного `Sample`
  Ожидаемый ввод: `sample_id` (`long`), `N` (`int > 0`)

- `cust_show <event_id>`
  Вывести одно событие передачи по идентификатору
  Ожидаемый ввод: `event_id` (`long`)

- `cust_update <event_id>`
  Обновить последнее событие передачи для образца
  Ожидаемый ввод:
  `event_id` (`long`), затем интерактивно:
  `from` (`string`), `to` (`string`), `location` (`string`), `comment` (`string`, необязательное поле)

- `cust_remove <event_id>`
  Удалить последнее событие передачи для образца
  Ожидаемый ввод: `event_id` (`long`)

- `cust_check <sample_id>`
  Показать текущего владельца образца
  Ожидаемый ввод: `sample_id` (`long`)

- `cust_export <sample_id>`
  Вывести цепочку передачи образца в текстовом виде
  Ожидаемый ввод: `sample_id` (`long`)

## Особенности логики

- `Sample` нельзя удалить, если с ним связаны `Seal` или `CustodyEvent`
- `BROKEN`-пломбу нельзя редактировать
- для `CustodyEvent` изменять и удалять можно только последнее событие для образца
