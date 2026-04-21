# 🎟️ Event Ticketing Platform with Payment Consistency

Sistema de venda de ingressos com foco em **consistência de pagamentos**, 
**processamento assíncrono** e **tratamento de concorrência**, 
simulando cenários reais de produção.

## 🚀 Visão Geral
Esta aplicação foi projetada para lidar com problemas reais encontrados em plataformas de eventos, como:
- Processamento de pagamentos via gateway externo
- Recebimento de webhooks duplicados
- Garantia de consistência entre pagamento e atualização de status do pedido

O sistema utiliza boas práticas de arquitetura para garantir **robustez**, **escalabilidade** e **observabilidade**.

## 🧠 Problemas Reais Resolvidos
### 🔁 Idempotência
Evita processamento duplicado de requisições críticas (ex: pagamento), garantindo que múltiplas chamadas com o mesmo contexto não causem efeitos colaterais.

### 📦 Outbox Pattern
Garante consistência entre banco de dados e eventos assíncronos, evitando perda de eventos em falhas de comunicação.

### 💳 Integração com Gateway de Pagamento
- Integração com EFI Bank (PIX em produção, cartão em sandbox)
- Tratamento de falhas externas
- Processamento de webhooks

### ⚔️ Concorrência
Preparado para cenários de alta concorrência, como múltiplas tentativas de compra do mesmo ingresso.
- Controle de estado de pedidos
- Estratégias para evitar inconsistências

### 🔍 Observabilidade
- Logs estruturados
- Uso de `traceId` para rastreamento de requisições
- Facilidade para debugging de falhas em produção

### 🏗️ Arquitetura
A aplicação segue os princípios de **Clean Architecture + DDD (Domain-Driven Design)**:

domain/ -> regras de negócio puras, entidades, value objects

application/ -> casos de uso e orquestração

infrastructure/ -> integração com banco, APIs externas, controllers

#### Principais conceitos aplicados:
- Aggregates
- Value Objects
- Use Cases
- Gateways (abstração de infraestrutura)
- Separação clara de responsabilidades

## 🔄 Fluxo de Pagamento
1. Usuário cria um pedido
2. Sistema inicia o pagamento (PIX ou cartão)
3. Gateway externo processa o pagamento
4. Webhook é recebido pelo sistema
5. Sistema valida idempotência da requisição
6. Pedido é atualizado (PENDING -> APPROVED/FAILED)
7. Tickets são liberados conforme status

## ⚙️ Tecnologias Utilizadas
- Java 21
- Spring Boot
- Gradle
- Docker / Docker Compose
- GitHub Actions
- PostgreSQL
- EFI Bank (PIX / Cartão)
- NGROK (para testes de webhook)

## 🐳 Execução do Projeto
### Pré-requisitos
- Java 21
- Docker + Docker Compose

### 🔧 Rodando localmente
```shell
git clone https://github.com/Kaua3045/events-platform.git 
cd events-platform 
./gradlew build 
cp .env.local .env 
./gradlew bootRun
```

API disponível em:

`http://localhost:8081/api`

### 🐳 Rodando com Docker + Sandbox de Pagamento
```shell
docker-compose -f docker/sandbox/observability/docker-compose.yml up -d 
docker-compose -f docker-compose-dev.yml up -d
```

> 💡 Dica: utiliza NGROK para expor sua aplicação local e permitir comunicação com o gateway de pagamento.

## 📊 Observabilidade
A aplicação possui suporte para:

- Logs estruturados
- Rastreamento por traceId
- Debug facilitado de falhas em pagamentos

Mais detalhes em: [Observabilidade](/doc/observability.md)

## 🔐 Idempotência
O sistema implementa proteção contra requisições duplicadas utilizando estratégias de idempotência.
Mais detalhes em: [Idempotência](/doc/idempotency-key.md)

## 🧪 Cenários Simulados
O sistema foi pensado para simular cenários reais como:
- Webhooks duplicados do gateway
- Falhas de comunicação externa
- Processamento assíncrono com consistência garantida

## 📌 Diferenciais Técnicos
- Integração real com gateway de pagamento
- Uso de padrões como Outbox e Idempotência
- Arquitetura baseada em DDD
- Preparado para cenários de produção
- Foco em problemas reais de backend

## 📈 Possíveis Evoluções
- Separação do serviço de autenticação (Auth Server dedicado)
- Escala horizontal com mensageria (Kafka/RabbitMQ)
- Cache distribuído
- Rate limiting e proteção contra abuso
- Monitoramento com ferramentas externas melhorado (Prometheus/Grafana)
- Testes de webhook duplicados e falhas de comunicação
- Implementação de testes de carga para validar comportamento sob alta concorrência

## 🤝 Contribuição
Para contribuir, veja: [CONTRIBUTING](/doc/CONTRIBUTING.md)

## 📄 Changelog
Para ver as últimas alterações do projeto, acesse: [Changelog](/doc/changelog.md)

## 👨‍💻 Autor
Desenvolvido por Kauã Pereira

Backend Developer focado em sistemas distribuídos e alta consistência