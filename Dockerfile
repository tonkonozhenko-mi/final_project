FROM node:8.11.2-alpine as build
WORKDIR /usr/src/app
COPY package*.json ./
RUN npm i
COPY . .
RUN npm run build

FROM nginx:1.17.1-alpine
WORKDIR /usr/share/nginx/html
RUN rm -rf ./*
COPY --from=build /usr/src/app/dist/eSchool .

ARG BASEURL
ENV baseUrl=$BASEURL
RUN sed -i 's|https://fierce-shore-32592.herokuapp.com|http://'$BASEURL'|g' /usr/share/nginx/html/main.js
