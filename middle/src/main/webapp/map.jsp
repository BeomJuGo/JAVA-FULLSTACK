<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<title>지도 보기</title>
<script type="text/javascript" src="https://openapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=uf38w3wc70"></script>
</head>
<body>
<div id="map" style="width:100%;height:800px;"></div>
<script>
const map = new naver.maps.Map('map', {
    center: new naver.maps.LatLng(37.3595704, 127.105399),
    zoom: 10
});

fetch('<%=request.getContextPath()%>/store/list.do')
    .then(res => res.json())
    .then(data => {
        data.items.forEach(item => {
            const marker = new naver.maps.Marker({
                position: new naver.maps.LatLng(item.latitude, item.longitude),
                map: map
            });
            const info = new naver.maps.InfoWindow({
                content: `<div style="padding:10px;"><b>${item.name}</b><br/>평점: ${item.rating}<br/>${item.address}</div>`
            });
            naver.maps.Event.addListener(marker, 'click', () => {
                info.open(map, marker);
            });
        });
    });
</script>
</body>
</html>
