import 'package:dio/dio.dart';
import '../../../core/network/dio_client.dart';
import '../models/admin_models.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final adminRepositoryProvider = Provider((ref) => AdminRepository(ref.read(dioProvider)));

class AdminRepository {
  final Dio _dio;

  AdminRepository(this._dio);

  Future<List<AdminProperty>> getMyProperties() async {
    final response = await _dio.get('/admin/properties');
    return (response.data as List).map((e) => AdminProperty.fromJson(e)).toList();
  }

  Future<void> updateInventory({
    required String roomTypeId,
    required String ratePlanId,
    required DateTime startDate,
    required DateTime endDate,
    required int totalCount,
    required int rateCents,
  }) async {
    await _dio.post('/admin/inventory/bulk-update', data: {
      'roomTypeId': roomTypeId,
      'ratePlanId': ratePlanId,
      'startDate': startDate.toIso8601String().split('T')[0],
      'endDate': endDate.toIso8601String().split('T')[0],
      'totalCount': totalCount,
      'rateCents': rateCents,
      'stopSell': false,
    });
  }
}
