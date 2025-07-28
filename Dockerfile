FROM --platform=linux/amd64 openjdk:11

WORKDIR /app

COPY . .

RUN mkdir -p classes && javac -cp "lib/*" -d classes src/pdf_outline.java


CMD ["java", "-cp", "lib/*:classes", "pdf_outline"]
