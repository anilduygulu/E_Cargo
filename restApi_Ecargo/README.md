# README #

This README would normally document whatever steps are necessary to get your application up and running.

* v1 içindeki index.php ye methodlar eklenicek, api kendisi URL ler oluşturucak.
* include klasöründeki php dosyalarını Database bağlantısına göre değiştir!
* libs klasörü Slim Framework dosyalarını içeriyor.

* slim framework çalışabilmesi için komut satırından projenin olduğu dosyaya gidip
* php -S localhost:8888 
* komutunun çalıştırması gerekiyor
* proje http://localhost:8888
* linki altında çalışmaktadır fakat browserler ile
* http://localhost:8888 ve altındaki klasör/dosyalar erişelemez
* gerekli methodları mesela login için
* http://localhost:8888/v1/login
* linkine POST ile istekte bulunur ve gerekli bilgiler (useremail,userpassword) aktarılır.
* service JSON döndürmekte ve örnek olarak

* başarılı bir giriş sonunda json şu bilgileri içermektedir
* hata durumu(boolean),username,usermail,userpassword,apikey ve kullanıcının oluşturulduğu tarih

* başarısız bir giriş sonucunda ise;
* hata durumu(boolean) ve An error occurred. Please try again
* diye bir json formatıyla karşılaşıcaksınız.
