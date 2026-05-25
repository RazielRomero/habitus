
FROM gradle:8.10-jdk17 AS builder

WORKDIR /home/gradle/project

COPY --chown=gradle:gradle . .

RUN chmod +x gradlew

# Compilar el target JS para producción (genera los archivos optimizados estáticos)
# Usamos --no-daemon para evitar procesos en segundo plano persistentes en el contenedor de compilación
RUN ./gradlew :composeApp:jsBrowserDistribution --no-daemon

#Servidor web ligero
FROM nginx:alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=builder /home/gradle/project/composeApp/build/dist/js/productionExecutable /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
