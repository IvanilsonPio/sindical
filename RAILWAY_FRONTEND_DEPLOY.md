# Deploy do Frontend (Angular) no Railway

Guia para fazer deploy do frontend Angular no Railway.

## 🎯 Opções de Deploy

Você tem 2 opções para o frontend:

### Opção 1: Frontend Separado no Railway (Recomendado)
- Frontend e backend como serviços separados
- Mais flexível e escalável
- Custo: ~$5-10/mês total

### Opção 2: Frontend em CDN (Vercel/Netlify)
- Frontend em CDN gratuito
- Backend no Railway
- Custo: ~$5/mês (só backend)

## 📦 Opção 1: Deploy no Railway

### 1. Adicionar Serviço Frontend

No seu projeto Railway:

1. Clique em "+ New"
2. Selecione "GitHub Repo"
3. Selecione o mesmo repositório
4. Configure o Root Directory: `frontend`

### 2. Configurar Variáveis de Ambiente

No serviço do frontend, adicione:

```bash
# URL do backend (use a URL pública do Railway)
API_URL=${{Backend.RAILWAY_PUBLIC_DOMAIN}}
```

### 3. Atualizar nginx.conf

O arquivo `frontend/nginx.conf` já está configurado, mas verifique se o proxy está correto:

```nginx
location /api {
    proxy_pass http://backend:8080;  # Para Railway, use a URL pública
    # ...
}
```

Para Railway, atualize para:

```nginx
location /api {
    proxy_pass https://${{Backend.RAILWAY_PUBLIC_DOMAIN}};
    # ...
}
```

### 4. Deploy

O Railway fará deploy automaticamente usando o `Dockerfile` do frontend.

## 🌐 Opção 2: Deploy no Vercel (Gratuito)

### 1. Preparar para Vercel

Crie `frontend/vercel.json`:

```json
{
  "buildCommand": "npm run build -- --configuration production",
  "outputDirectory": "dist/frontend/browser",
  "framework": "angular",
  "rewrites": [
    {
      "source": "/api/:path*",
      "destination": "https://seu-backend.up.railway.app/api/:path*"
    },
    {
      "source": "/(.*)",
      "destination": "/index.html"
    }
  ]
}
```

### 2. Deploy no Vercel

```bash
# Instale o Vercel CLI
npm i -g vercel

# Na pasta frontend
cd frontend

# Deploy
vercel

# Para produção
vercel --prod
```

Ou conecte via GitHub:
1. Acesse https://vercel.com
2. Import Git Repository
3. Selecione a pasta `frontend`
4. Configure a variável `API_URL` com a URL do backend Railway

## 🚀 Opção 3: Deploy no Netlify (Gratuito)

### 1. Preparar para Netlify

Crie `frontend/netlify.toml`:

```toml
[build]
  command = "npm run build -- --configuration production"
  publish = "dist/frontend/browser"

[[redirects]]
  from = "/api/*"
  to = "https://seu-backend.up.railway.app/api/:splat"
  status = 200
  force = true

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200
```

### 2. Deploy no Netlify

```bash
# Instale o Netlify CLI
npm i -g netlify-cli

# Na pasta frontend
cd frontend

# Deploy
netlify deploy

# Para produção
netlify deploy --prod
```

Ou conecte via GitHub:
1. Acesse https://netlify.com
2. Add new site → Import from Git
3. Selecione o repositório
4. Base directory: `frontend`
5. Build command: `npm run build -- --configuration production`
6. Publish directory: `dist/frontend/browser`

## 🔧 Configurar CORS no Backend

Para qualquer opção, você precisa configurar CORS no backend.

Crie/atualize `src/main/java/com/sindicato/config/CorsConfig.java`:

```java
package com.sindicato.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
```

Adicione no Railway (variáveis do backend):

```bash
CORS_ALLOWED_ORIGINS=https://seu-frontend.vercel.app,https://seu-frontend.netlify.app
```

## 📊 Comparação de Custos

| Opção | Custo/mês | Vantagens | Desvantagens |
|-------|-----------|-----------|--------------|
| Railway (ambos) | $5-10 | Tudo junto, simples | Um pouco mais caro |
| Railway + Vercel | $5 | Frontend grátis, rápido | Configuração CORS |
| Railway + Netlify | $5 | Frontend grátis, rápido | Configuração CORS |

## 🎯 Recomendação

Para começar: **Railway + Vercel**
- Backend no Railway ($5/mês)
- Frontend no Vercel (grátis)
- Total: $5/mês
- Deploy automático em ambos
- CDN global para frontend (mais rápido)

## 🔄 Atualizar environment.ts

Atualize `frontend/src/environments/environment.prod.ts`:

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://seu-backend.up.railway.app/api'
};
```

Ou use variável de ambiente no build:

```typescript
export const environment = {
  production: true,
  apiUrl: process.env['API_URL'] || 'https://seu-backend.up.railway.app/api'
};
```

## ✅ Checklist Final

- [ ] Backend deployado no Railway
- [ ] PostgreSQL configurado
- [ ] Variáveis de ambiente configuradas
- [ ] Frontend deployado (Railway/Vercel/Netlify)
- [ ] CORS configurado no backend
- [ ] API_URL configurada no frontend
- [ ] Usuário admin criado
- [ ] Teste de login funcionando
- [ ] Upload de arquivos funcionando

---

**Próximo passo**: Escolha uma opção e siga o guia correspondente!
