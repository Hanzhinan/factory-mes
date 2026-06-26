<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>3D库房库位可视化</title>
    <style>
        body { margin: 0; overflow: hidden; background: #4682B4; }
        #label { position: absolute; padding: 8px; background: rgba(0,0,0,0.7); color: white; border-radius: 4px; font-size: 12px; pointer-events: none; display: none; z-index: 1000; }
        #toolbar { position: absolute; top: 10px; left: 10px; background: rgba(255,255,255,0.9); padding: 10px; border-radius: 8px; z-index: 1000; }
        .btn { padding: 6px 12px; margin: 4px; background: #3398DB; color: white; border: none; border-radius: 4px; cursor: pointer; }
        .btn:hover { background: #258cd1; }
        #info { position: absolute; bottom: 10px; left: 10px; background: rgba(0,0,0,0.7); color: white; padding: 10px; border-radius: 8px; font-size: 12px; z-index: 1000; }
    </style>
    <script type="text/javascript" src="${request.contextPath}/lib/ThreeJs/js/three.js"></script>
    <script type="text/javascript" src="${request.contextPath}/lib/ThreeJs/js/OrbitControls.js"></script>
    <script type="text/javascript" src="${request.contextPath}/lib/jquery/jquery.min.js"></script>
</head>
<body>
<div id="toolbar">
    <button class="btn" onclick="loadWarehouseData()">加载库位数据</button>
    <button class="btn" onclick="clearScene()">清空场景</button>
    <button class="btn" onclick="toggleView('top')">俯视图</button>
    <button class="btn" onclick="toggleView('front')">正视图</button>
    <button class="btn" onclick="toggleView('isometric')">等轴测</button>
</div>
<div id="label"></div>
<div id="container"></div>
<div id="info">数据统计: 等待加载...</div>

<script>
    var scene, camera, renderer, controls;
    var warehouseData = [];
    var raycaster = new THREE.Raycaster();
    var mouse = new THREE.Vector2();

    function init() {
        scene = new THREE.Scene();
        
        camera = new THREE.PerspectiveCamera(45, window.innerWidth / window.innerHeight, 0.1, 10000);
        camera.position.set(100, 80, 100);

        renderer = new THREE.WebGLRenderer({ antialias: true });
        renderer.setSize(window.innerWidth, window.innerHeight);
        renderer.setClearColor(0x4682B4);
        renderer.shadowMap.enabled = true;
        document.getElementById('container').appendChild(renderer.domElement);

        controls = new THREE.OrbitControls(camera, renderer.domElement);
        controls.enableDamping = true;
        controls.dampingFactor = 0.5;
        controls.minDistance = 20;
        controls.maxDistance = 500;

        addLight();
        addFloor();
        addGrid();

        renderer.domElement.addEventListener('mousemove', onMouseMove, false);
        renderer.domElement.addEventListener('click', onClick, false);
        window.addEventListener('resize', onWindowResize, false);

        animate();
        loadWarehouseData();
    }

    function addLight() {
        var ambientLight = new THREE.AmbientLight(0xffffff, 0.6);
        scene.add(ambientLight);

        var directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
        directionalLight.position.set(50, 100, 50);
        directionalLight.castShadow = true;
        scene.add(directionalLight);
    }

    function addFloor() {
        var geometry = new THREE.PlaneGeometry(300, 300);
        var material = new THREE.MeshStandardMaterial({ color: 0x888888 });
        var floor = new THREE.Mesh(geometry, material);
        floor.rotation.x = -Math.PI / 2;
        floor.receiveShadow = true;
        scene.add(floor);
    }

    function addGrid() {
        var gridHelper = new THREE.GridHelper(300, 30, 0xcccccc, 0xeeeeee);
        gridHelper.position.y = 0.01;
        scene.add(gridHelper);
    }

    function loadWarehouseData() {
        console.log('开始加载库位数据...');
        $.ajax({
            url: '${request.contextPath}/basedata/warehouseLocation/list-all',
            type: 'GET',
            success: function(response) {
                console.log('接口返回:', response);
                if (response.code === 0 && response.data) {
                    warehouseData = response.data;
                    console.log('库位数据:', warehouseData);
                    clearScene();
                    renderWarehouse(warehouseData);
                    updateInfo(warehouseData);
                    alert('加载成功！共 ' + warehouseData.length + ' 个库位');
                } else {
                    alert('加载失败: ' + (response.msg || '未知错误'));
                    console.error('加载失败:', response);
                }
            },
            error: function(xhr, status, error) {
                alert('加载库位数据失败: ' + error);
                console.error('AJAX错误:', xhr, status, error);
            }
        });
    }

    function updateInfo(data) {
        var areas = {};
        var shelves = {};
        var statusCount = {0:0, 1:0, 2:0};
        
        data.forEach(function(item) {
            areas[item.areaCode || 'DEFAULT'] = true;
            shelves[item.shelfId || 'UNKNOWN'] = true;
            statusCount[item.status || 0]++;
        });

        var info = document.getElementById('info');
        info.innerHTML = 
            '数据统计: 共 ' + data.length + ' 个库位<br>' +
            '库区: ' + Object.keys(areas).length + ' 个<br>' +
            '货架: ' + Object.keys(shelves).length + ' 个<br>' +
            '可用: ' + statusCount[0] + ' | 占用: ' + statusCount[1] + ' | 禁用: ' + statusCount[2];
    }

    function renderWarehouse(data) {
        if (!data || data.length === 0) {
            console.log('没有数据可渲染');
            return;
        }

        var areaGroups = {};
        data.forEach(function(item) {
            var areaCode = item.areaCode || 'DEFAULT';
            if (!areaGroups[areaCode]) {
                areaGroups[areaCode] = [];
            }
            areaGroups[areaCode].push(item);
        });
        console.log('库区分组:', areaGroups);

        var areaOffsetX = -80;
        var areaOffsetZ = -80;
        var areaIndex = 0;

        for (var areaCode in areaGroups) {
            var areaData = areaGroups[areaCode];
            renderArea(areaCode, areaData, areaOffsetX + (areaIndex % 3) * 60, areaOffsetZ + Math.floor(areaIndex / 3) * 60);
            areaIndex++;
        }
    }

    function renderArea(areaCode, areaData, offsetX, offsetZ) {
        var areaGeometry = new THREE.BoxGeometry(50, 1, 40);
        var areaMaterial = new THREE.MeshStandardMaterial({ color: 0x87CEEB, transparent: true, opacity: 0.3 });
        var areaBox = new THREE.Mesh(areaGeometry, areaMaterial);
        areaBox.position.set(offsetX, 0.5, offsetZ);
        areaBox.name = 'AREA_' + areaCode;
        areaBox.userData = { type: 'area', areaCode: areaCode };
        scene.add(areaBox);

        var canvas = document.createElement('canvas');
        canvas.width = 256;
        canvas.height = 64;
        var ctx = canvas.getContext('2d');
        ctx.fillStyle = 'rgba(0,0,0,0.5)';
        ctx.fillRect(0, 0, 256, 64);
        ctx.fillStyle = '#ffffff';
        ctx.font = 'bold 24px Arial';
        ctx.textAlign = 'center';
        ctx.fillText('库区: ' + areaCode, 128, 40);
        var texture = new THREE.CanvasTexture(canvas);
        var spriteMaterial = new THREE.SpriteMaterial({ map: texture });
        var sprite = new THREE.Sprite(spriteMaterial);
        sprite.scale.set(20, 5, 1);
        sprite.position.set(offsetX, 10, offsetZ);
        scene.add(sprite);

        var shelfMap = {};
        areaData.forEach(function(item) {
            var shelfId = item.shelfId || 'SHELF_' + (item.locationCode || 'unknown');
            if (!shelfMap[shelfId]) {
                shelfMap[shelfId] = [];
            }
            shelfMap[shelfId].push(item);
        });
        console.log('货架分组:', shelfMap);

        var shelfIndex = 0;
        for (var shelfId in shelfMap) {
            var shelfData = shelfMap[shelfId];
            var row = shelfIndex % 4;
            var col = Math.floor(shelfIndex / 4);
            renderShelf(shelfId, shelfData, offsetX - 20 + row * 15, offsetZ - 15 + col * 12);
            shelfIndex++;
        }
    }

    function renderShelf(shelfId, shelfData, x, z) {
        var shelfWidth = 10;
        var shelfHeight = 15;
        var shelfDepth = 6;
        var layerHeight = 3;

        var frameGeometry = new THREE.BoxGeometry(shelfWidth, shelfHeight, shelfDepth);
        var frameMaterial = new THREE.MeshStandardMaterial({ color: 0x708090 });
        var frame = new THREE.Mesh(frameGeometry, frameMaterial);
        frame.position.set(x, shelfHeight / 2, z);
        frame.castShadow = true;
        frame.name = 'SHELF_' + shelfId;
        frame.userData = { type: 'shelf', shelfId: shelfId, data: shelfData };
        scene.add(frame);

        for (var i = 1; i < 5; i++) {
            var shelfGeometry = new THREE.BoxGeometry(shelfWidth - 1, 0.5, shelfDepth - 1);
            var shelfMaterial = new THREE.MeshStandardMaterial({ color: 0x8B4513 });
            var shelf = new THREE.Mesh(shelfGeometry, shelfMaterial);
            shelf.position.set(x, i * layerHeight, z);
            shelf.castShadow = true;
            scene.add(shelf);
        }

        shelfData.forEach(function(item) {
            var layer = item.shelfLayer || 1;
            var status = item.status || 0;
            
            var color = 0x90EE90;
            if (status === 1) color = 0xFF6347;
            else if (status === 2) color = 0xD3D3D3;

            var locationGeometry = new THREE.BoxGeometry(shelfWidth - 2, layerHeight - 0.5, shelfDepth - 2);
            var locationMaterial = new THREE.MeshStandardMaterial({ 
                color: color, 
                transparent: true, 
                opacity: 0.8,
                emissive: color,
                emissiveIntensity: 0.2
            });
            var location = new THREE.Mesh(locationGeometry, locationMaterial);
            location.position.set(x, layer * layerHeight, z);
            location.castShadow = true;
            location.name = 'LOCATION_' + (item.locationCode || 'unknown');
            location.userData = { 
                type: 'location', 
                data: item 
            };
            scene.add(location);
        });
    }

    function clearScene() {
        var objectsToRemove = [];
        scene.children.forEach(function(child) {
            if (child.name && (child.name.startsWith('AREA_') || 
                              child.name.startsWith('SHELF_') || 
                              child.name.startsWith('LOCATION_') ||
                              child instanceof THREE.Sprite)) {
                objectsToRemove.push(child);
            }
        });
        objectsToRemove.forEach(function(obj) {
            scene.remove(obj);
        });
        warehouseData = [];
    }

    function toggleView(viewType) {
        if (viewType === 'top') {
            camera.position.set(0, 200, 0);
            camera.lookAt(0, 0, 0);
        } else if (viewType === 'front') {
            camera.position.set(0, 50, 150);
            camera.lookAt(0, 0, 0);
        } else if (viewType === 'isometric') {
            camera.position.set(100, 80, 100);
            camera.lookAt(0, 0, 0);
        }
    }

    function onMouseMove(event) {
        mouse.x = (event.clientX / window.innerWidth) * 2 - 1;
        mouse.y = -(event.clientY / window.innerHeight) * 2 + 1;

        raycaster.setFromCamera(mouse, camera);
        var intersects = raycaster.intersectObjects(scene.children);

        if (intersects.length > 0) {
            var obj = intersects[0].object;
            if (obj.userData.type === 'location' && obj.userData.data) {
                var data = obj.userData.data;
                var label = document.getElementById('label');
                label.innerHTML = 
                    '库位编号: ' + (data.locationCode || '-') + '<br>' +
                    '库位名称: ' + (data.locationName || '-') + '<br>' +
                    '库房: ' + (data.warehouseName || '-') + '<br>' +
                    '状态: ' + (data.status === 0 ? '可用' : data.status === 1 ? '占用' : '禁用') + '<br>' +
                    '容量: ' + (data.capacity || 0) + '<br>' +
                    '当前数量: ' + (data.currentQty || 0);
                label.style.display = 'block';
                label.style.left = event.clientX + 10 + 'px';
                label.style.top = event.clientY + 10 + 'px';
                document.body.style.cursor = 'pointer';
                return;
            }
        }
        document.getElementById('label').style.display = 'none';
        document.body.style.cursor = 'default';
    }

    function onClick(event) {
        mouse.x = (event.clientX / window.innerWidth) * 2 - 1;
        mouse.y = -(event.clientY / window.innerHeight) * 2 + 1;

        raycaster.setFromCamera(mouse, camera);
        var intersects = raycaster.intersectObjects(scene.children);

        if (intersects.length > 0) {
            var obj = intersects[0].object;
            if (obj.userData.type === 'location' && obj.userData.data) {
                var data = obj.userData.data;
                alert('库位详情:\n' +
                    '编号: ' + (data.locationCode || '-') + '\n' +
                    '名称: ' + (data.locationName || '-') + '\n' +
                    '库房: ' + (data.warehouseName || '-') + '\n' +
                    '状态: ' + (data.status === 0 ? '可用' : data.status === 1 ? '占用' : '禁用') + '\n' +
                    '容量: ' + (data.capacity || 0) + '\n' +
                    '当前数量: ' + (data.currentQty || 0));
            }
        }
    }

    function onWindowResize() {
        camera.aspect = window.innerWidth / window.innerHeight;
        camera.updateProjectionMatrix();
        renderer.setSize(window.innerWidth, window.innerHeight);
    }

    function animate() {
        requestAnimationFrame(animate);
        controls.update();
        renderer.render(scene, camera);
    }

    init();
</script>
</body>
</html>