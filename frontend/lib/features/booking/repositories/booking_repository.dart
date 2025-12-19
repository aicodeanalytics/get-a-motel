import 'package:dio/dio.dart';
import '../../../core/network/dio_client.dart';
import '../models/booking_models.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final bookingRepositoryProvider = Provider((ref) => BookingRepository(ref.read(dioProvider)));

class BookingRepository {
  final Dio _dio;

  BookingRepository(this._dio);

  Future<BookingResponse> createBooking(CreateBookingRequest request) async {
    final response = await _dio.post('/bookings', data: request.toJson());
    return BookingResponse.fromJson(response.data);
  }
}
