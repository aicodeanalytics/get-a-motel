import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/auth/auth_service.dart';
import 'package:firebase_auth/firebase_auth.dart';

final dioProvider = Provider((ref) {
  final dio = Dio(BaseOptions(
    baseUrl: 'http://10.0.2.2:8080', // Update for prod
    connectTimeout: const Duration(seconds: 5),
    receiveTimeout: const Duration(seconds: 3),
  ));

  dio.interceptors.add(InterceptorsWrapper(
    onRequest: (options, handler) async {
      // 1. Add Auth Token
      final user = FirebaseAuth.instance.currentUser;
      if (user != null) {
        final token = await user.getIdToken();
        options.headers['Authorization'] = 'Bearer $token';
      }

      // 2. Add Tenant ID (if applicable)
      // Note: In MVP, tenants might be determined by URL or profile
      options.headers['X-Tenant-ID'] = '00000000-0000-0000-0000-000000000000'; // Placeholder
      
      return handler.next(options);
    },
  ));

  return dio;
});
