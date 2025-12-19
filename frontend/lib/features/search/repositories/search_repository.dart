import 'package:dio/dio.dart';
import '../../../core/network/dio_client.dart';
import '../models/property.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final searchRepositoryProvider = Provider((ref) => SearchRepository(ref.read(dioProvider)));

class SearchRepository {
  final Dio _dio;

  SearchRepository(this._dio);

  Future<List<PropertySearchResult>> search({
    String? city,
    String? state,
    required DateTime checkIn,
    required DateTime checkOut,
    int guests = 1,
  }) async {
    final response = await _dio.get('/properties/search', queryParameters: {
      if (city != null) 'city': city,
      if (state != null) 'state': state,
      'check_in': checkIn.toIso8601String().split('T')[0],
      'check_out': checkOut.toIso8601String().split('T')[0],
      'guests': guests,
    });

    return (response.data as List)
        .map((e) => PropertySearchResult.fromJson(e))
        .toList();
  }
}
