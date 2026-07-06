# SeasonRPG Demo — Guia de Execução

Demo jogável do **núcleo** de um servidor Survival RPG por temporadas.
Roda em um **Paper 1.21.4 limpo** — não precisa de nenhum plugin externo.

Os quatro pilares que esta demo prova:

1. **Temporada** — relógio ao vivo (boss bar no topo) com semana atual, tempo restante e objetivo.
2. **Vidas limitadas** — 5 vidas por jogador; a **última vida** dispara um anúncio dramático para todo o servidor; perder a última elimina (vira espectador).
3. **Classes** — Guerreiro (corpo a corpo) vs Arqueiro (à distância), cada um com **uma habilidade** que muda o jeito de jogar.
4. **Evento dinâmico** — a "Invasão": aviso com contagem, horda que surge no mundo, defesa coletiva e recompensa.

---

## 1. Pré-requisitos

- **Java 21** (JDK). Já instalado neste ambiente (Eclipse Temurin 21).
- Cliente **Minecraft Java 1.21.4** para conectar.

## 2. Compilar o plugin

Na pasta do projeto, rode:

```powershell
./build.ps1
```

Isso baixa o Paper 1.21.4 (para `server/`), compila as fontes e gera:

```
build/SeasonRPGDemo-0.1.0.jar
```

## 3. Preparar o servidor

```powershell
# criar pastas e aceitar a EULA
New-Item -ItemType Directory -Force server/plugins | Out-Null
Copy-Item build/SeasonRPGDemo-0.1.0.jar server/plugins/
"eula=true" | Out-File server/eula.txt -Encoding ascii
```

## 4. Iniciar o servidor

```powershell
cd server
& "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot\bin\java.exe" -Xmx2G -jar paper-1.21.4.jar nogui
```

Na primeira vez ele gera o mundo e desliga; rode de novo se necessário.
Deixe a janela aberta — é o console do servidor.

## 5. Conectar

- Abra o Minecraft **1.21.4** → Multijogador → Adicionar servidor → `localhost`.
- Para o cliente entrar de fora, exponha a porta 25565 com **playit.gg**
  (grátis) ou **ngrok** e envie o endereço gerado.

---

## Como testar cada sistema (roteiro de 5 min)

| O que fazer | O que você vê |
|---|---|
| Entrar no servidor | Boss bar "Temporada 1 · Semana 1/8 · termina em..." e action bar com vidas/classe |
| `/class` | Menu com Guerreiro e Arqueiro; clique para escolher e receber o kit |
| Segurar a arma e **shift + clique direito** (ou `/ability`) | Guerreiro dá uma investida; Arqueiro dispara um tiro certeiro (cooldown 12s) |
| Morrer algumas vezes (ex.: cair, mob) | Perde 1 vida por morte; na última, anúncio "ULTIMA VIDA" no servidor |
| `/event start` (como OP) | Aviso de invasão + horda de zumbis; matá-los dá o "Espólio da Invasão" e pode devolver 1 vida |
| `/season` / `/lives` | Resumo da temporada e das vidas |

### Comandos de administração (para demonstrar rápido)

Precisam de OP (`op SEU_NICK` no console):

```
/seasonadmin setweek 8      # pula para a última semana (barra quase cheia)
/seasonadmin reset          # reinicia o relógio da temporada
/seasonadmin resetlives     # devolve todos às 5 vidas
/seasonadmin givelife <nick> <n>
/event start                # dispara uma invasão agora
/event stop                 # encerra a invasão
```

Dica: para ver a "última vida" na hora, `/seasonadmin givelife <nick> -3` até
sobrar 1, depois morra uma vez.

---

## Ajustes rápidos (sem recompilar)

Edite `server/plugins/SeasonRPGDemo/config.yml` e rode `/reload confirm` ou
reinicie:

- `season.minutes-per-week` — quão rápido a temporada avança (demo: 10 min).
- `lives.starting` — vidas iniciais.
- `event.wave-size`, `event.warning-seconds`, `event.auto` — o evento.
- `classes.ability-cooldown-seconds`.

---

## O que é demo e o que vem no build pago

**Nesta demo (núcleo jogável):** temporada, vidas + narrativa, 2 classes com
habilidade, 1 evento dinâmico completo.

**Fora da demo (Season 1 paga):** curva de 8 semanas com dificuldade por bioma,
chefe final coordenado, invasores como chefes do **MythicMobs**, missões
semanais, economia, proteção de terreno, persistência entre temporadas e
integração com os plugins premium que você já possui.
