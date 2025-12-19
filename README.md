# LabsTV Backend

Backend da aplicação **LabsTV**, uma implementação da API Spec "Rei dos Canais". Este projeto fornece uma API RESTful desenvolvida em Spring Boot para gerenciar e listar canais de TV, eventos esportivos e placares ao vivo.

## 🚀 Tecnologias Utilizadas

- **Java 17+**
- **Spring Boot 3** (Web, Data JPA, Scheduling)
- **H2 Database** (Banco de dados em memória para desenvolvimento rápido)
- **RestTemplate** (Consumo de APIs externas)

## 📋 Funcionalidades Principais

1. **Catálogo de Canais**: Inicialização automática de uma lista de canais (Mock) com suporte a categorias e URLs de stream/logo.
2. **Eventos Esportivos**: Gerenciamento de agenda de jogos com suporte a múltiplos embeds (players) por evento.
3. **Placares ao Vivo (Live Scores)**: Integração automática com a API da ESPN para sincronizar resultados de Futebol, Basquete, NFL, etc.
4. **Busca Unificada**: Endpoint `/search` que pesquisa simultaneamente em canais e eventos.
5. **Proxy Interno**: Utilitário para contornar restrições de CORS (Cross-Origin Resource Sharing) no frontend.
6. **Configurações Dinâmicas**: Endpoints que servem JSONs de configuração para o frontend (Ligas, Logos, Wiki data).

## 🛠️ Como Executar

### Pré-requisitos
- JDK 17 ou superior.
- Maven instalado.

### Passos
1. Clone o repositório.
2. Na raiz do projeto, execute:

```bash
mvn spring-boot:run
```

A aplicação iniciará na porta **8080**.

## 🔌 Documentação da API

### 📺 Canais
| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/channels` | Lista todos os canais. Aceita `?category=Nome` para filtrar. |
| `GET` | `/channels/{id}` | Detalhes de um canal específico. |
| `GET` | `/channels/categories` | Lista todas as categorias de canais disponíveis. |

### ⚽ Esportes e Eventos
| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/sports` | Lista eventos. Filtros: `?category=...` e `?status=...` (live, upcoming, ended). |
| `GET` | `/sports/{id}` | Detalhes de um evento específico. |
| `GET` | `/sports/categories` | Lista categorias de esportes. |
| `GET` | `/scores` | Retorna placares ao vivo da ESPN. Padrão: futebol. |

### 🔍 Utilitários
| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/search` | Busca unificada. Parâmetro obrigatório: `?q=termo`. |
| `GET` | `/proxy` | Proxy para requisições externas. Parâmetro: `?url=...`. |

### ⚙️ Configuração (Frontend)
- `/api/config/sports`: Retorna dados estáticos de ligas e times.
- `/api/config/wiki`: Retorna mapeamento para páginas da Wikipedia.

## 🔄 Sincronização Automática

O sistema possui um agendador (`ScoreService`) configurado para rodar a cada **5 minutos**. Ele consulta a API da ESPN para atualizar a lista de eventos esportivos no banco de dados local, garantindo que os jogos ao vivo e futuros estejam sempre atualizados.

## 📂 Estrutura de Dados (Mock)

Ao iniciar, a aplicação carrega automaticamente (`CommandLineRunner`) uma lista pré-definida de canais (Globo, ESPN, HBO, etc.) e um evento de teste para facilitar o desenvolvimento do frontend sem necessidade de configurar um banco de dados externo.
