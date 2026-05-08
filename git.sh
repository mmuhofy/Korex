#!/bin/bash

# 1. Mantık: Bilgileri al ve Git yapılandırmasını oluştur.
echo "--- GitHub Otomatik Yapılandırma (Token) ---"

# Kullanıcıdan bilgileri tek seferde topluyoruz
read -p "GitHub Kullanıcı Adı: " GIT_USER
read -p "GitHub Email: " GIT_EMAIL
# -s parametresi token yazarken ekranda görünmesini engeller (güvenlik için)
read -sp "GitHub Token (ghp_...): " GIT_TOKEN
echo -e "\n"

# 2. Eksiksiz Git Config Ayarları
git config --global user.name "$GIT_USER"
git config --global user.email "$GIT_EMAIL"
git config --global init.defaultBranch main
git config --global credential.helper store

# 3. Kimlik Bilgilerini Sisteme Yazma
# Bu satır, ileride şifre sormasını tamamen engeller.
echo "https://${GIT_USER}:${GIT_TOKEN}@github.com" > ~/.git-credentials
chmod 600 ~/.git-credentials

# 4. Hata Yönetimi ve Kontrol
if [ -f ~/.git-credentials ]; then
    echo "✅ Başarılı: Bilgiler kaydedildi."
    echo "💡 İpucu: Artık projelerini HTTPS linkiyle klonlayıp şifresiz pushlayabilirsin."
else
    echo "❌ Hata: Dosya yazılamadı, yetkilerini kontrol et."
fi

echo -e "\nSon durum:"
git config --list --global
