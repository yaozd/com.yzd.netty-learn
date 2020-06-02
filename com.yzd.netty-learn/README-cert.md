## 生成证书

## generate-cert.sh
- [https://github.com/yaozd/grpc-proxy.git](https://github.com/yaozd/grpc-proxy.git)
```
#!/bin/bash
# Based on a script which was written by Martijn Vermaat <martijn@vermaat.name>
set -euo pipefail
IFS=$'\n\t'

if [ $# -lt 1 ]; then
    echo "Usage: $0 <common name (or DNS name)> <DNS names or ip addresses...>"
    exit 1
fi

common_name="$1"
args="${@:2}"
config="$(mktemp)"

dnss=
ips=
for arg in ${args}; do
    if [[ "${arg}" =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
        ips+="${arg} "
    else
        dnss+="${arg} "
    fi
done

altnames=
subjectaltline=

i=0
for dns in ${dnss}; do
    i=$(($i+1))
    altnames+="DNS.${i} = ${dns}"$'\n'
    subjectaltline="subjectAltName = @alt_names"
done

i=0
for ip in ${ips}; do
    i=$(($i+1))
    altnames+="IP.${i} = ${ip}"$'\n'
    subjectaltline="subjectAltName = @alt_names"
done

cat >"${config}" <<EOF
[req]
distinguished_name = req_distinguished_name
x509_extensions = v3_ca
req_extensions = v3_req
prompt = no

[req_distinguished_name]
CN = ${common_name}

[v3_ca]
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid,issuer
basicConstraints = CA:TRUE
${subjectaltline}

[v3_req]
keyUsage = nonRepudiation, digitalSignature, keyEncipherment
basicConstraints = CA:FALSE
${subjectaltline}

[alt_names]
${altnames}
EOF

# Generate a ECDSA NIST P256 private key.
openssl ecparam -out cert.key.x509 -name prime256v1 -genkey

# Generate a self-signed certificate for the key.
openssl req -new -x509 -sha256 -nodes -key cert.key.x509 -out cert.crt -batch -config "${config}"

# Convert the key for PKCS8 format.
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in cert.key.x509 -out cert.key

# Remove the temporary key.
rm cert.key.x509

```


###　[sh脚本异常：/bin/sh^M:bad interpreter: No such file or directory](https://www.iteye.com/blog/qsfwy-1667189)
```
解决：
1 在windows下转换
利用一些编辑器如UltraEdit或EditPlus等工具先将脚本编码转换，再放到Linux中执行。转换方式如下（UltraEdit）：File-->Conversions-->DOS->UNIX即可。
sh脚本异常：/bin/sh^M:bad interpreter: No such file or directory - myswirl - 漩涡的窝
//
2 在Linux中转换
2.1 首先要确保文件有可执行权限
#sh>chmod a+x filename
2.2 然后修改文件格式
#sh>vi filename
2.3 利用如下命令查看文件格式
:set ff 或 :set fileformat
可以看到如下信息
fileformat=dos 或 fileformat=unix
2.4 利用如下命令修改文件格式
:set ff=unix 或 :set fileformat=unix
:wq (存盘退出)
2.5 最后再执行文件
#sh>./filename
```