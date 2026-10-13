// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "flutter_logs_yoer",
    platforms: [.iOS("9.0")],
    products: [.library(name: "flutter-logs-yoer", targets: ["flutter_logs_yoer"])],
    dependencies: [
        .package(name: "FlutterFramework", path: "../FlutterFramework"),
        .package(url: "https://github.com/weichsel/ZIPFoundation.git", from: "0.9.19")
    ],
    targets: [
        .target(
            name: "flutter_logs_yoer",
            dependencies: [
                .product(name: "FlutterFramework", package: "FlutterFramework"),
                .product(name: "ZIPFoundation", package: "ZIPFoundation")
            ]
        )
    ]
)
